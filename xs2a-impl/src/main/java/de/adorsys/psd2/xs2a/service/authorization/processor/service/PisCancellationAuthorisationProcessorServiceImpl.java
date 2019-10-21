/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisCommonDecoupledService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.consent.PisPsuDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.spi.payment.SpiPaymentServiceResolver;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD")
public class PisCancellationAuthorisationProcessorServiceImpl extends BaseAuthorisationProcessorService {
    private static final String UNSUPPORTED_ERROR_MESSAGE = "Current SCA status is not supported";

    private final List<PisScaAuthorisationService> services;
    private final Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    private final PaymentCancellationSpi paymentCancellationSpi;
    private final PisAspspDataService pisAspspDataService;
    private final Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiErrorMapper spiErrorMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final RequestProviderService requestProviderService;
    private final Xs2aUpdatePaymentAfterSpiService updatePaymentAfterSpiService;
    private final Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    private final PisCommonDecoupledService pisCommonDecoupledService;
    private final SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;
    private final SpiPaymentServiceResolver spiPaymentServiceResolver;
    private final PisPsuDataService pisPsuDataService;
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;

    @Override
    public void updateAuthorisation(AuthorisationProcessorRequest request, AuthorisationProcessorResponse response) {
        PisScaAuthorisationService authorizationService = getService(request.getScaApproach());
        authorizationService.updateCancellationAuthorisation(request.getUpdateAuthorisationRequest(), response);
    }

    @Override
    public AuthorisationProcessorResponse doScaReceived(AuthorisationProcessorRequest authorisationProcessorRequest) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        GetPisAuthorisationResponse pisAuthorisationResponse = (GetPisAuthorisationResponse) authorisationProcessorRequest.getAuthorisation();
        return request.isUpdatePsuIdentification() && pisAuthorisationResponse.getChosenScaApproach() != ScaApproach.DECOUPLED
                   ? applyIdentification(request)
                   : applyAuthorisation(request, pisAuthorisationResponse);
    }

    @Override
    public AuthorisationProcessorResponse doScaPsuIdentified(AuthorisationProcessorRequest authorisationProcessorRequest) {
        return doScaReceived(authorisationProcessorRequest);
    }

    @Override
    public AuthorisationProcessorResponse doScaPsuAuthenticated(AuthorisationProcessorRequest authorisationProcessorRequest) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        GetPisAuthorisationResponse pisAuthorisationResponse = (GetPisAuthorisationResponse) authorisationProcessorRequest.getAuthorisation();

        PaymentType paymentType = pisAuthorisationResponse.getPaymentType();
        String paymentProduct = pisAuthorisationResponse.getPaymentProduct();
        SpiPayment payment = xs2aToSpiPaymentMapper.mapToSpiPayment(pisAuthorisationResponse, paymentType, paymentProduct);

        if (isDecoupledApproach(request.getAuthorisationId(), request.getAuthenticationMethodId())) {
            xs2aPisCommonPaymentService.updateScaApproach(request.getAuthorisationId(), ScaApproach.DECOUPLED);
            return pisCommonDecoupledService.proceedDecoupledCancellation(request, payment, request.getAuthenticationMethodId());
        }

        return proceedEmbeddedApproach(request, payment, pisAuthorisationResponse);
    }

    @Override
    public AuthorisationProcessorResponse doScaMethodSelected(AuthorisationProcessorRequest authorisationProcessorRequest) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        GetPisAuthorisationResponse pisAuthorisationResponse = (GetPisAuthorisationResponse) authorisationProcessorRequest.getAuthorisation();

        PaymentType paymentType = pisAuthorisationResponse.getPaymentType();
        String paymentProduct = pisAuthorisationResponse.getPaymentProduct();
        SpiPayment payment = xs2aToSpiPaymentMapper.mapToSpiPayment(pisAuthorisationResponse, paymentType, paymentProduct);
        PsuIdData psuData = extractPsuIdData(request, pisAuthorisationResponse);
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(paymentId);

        String internalId = pisAspspDataService.getInternalPaymentIdByEncryptedString(paymentId);
        SpiScaConfirmation spiScaConfirmation = xs2aPisCommonPaymentMapper.buildSpiScaConfirmation(request, pisAuthorisationResponse.getPaymentId(), internalId, psuData);

        SpiResponse<SpiResponse.VoidResponse> spiResponse = paymentCancellationSpi.verifyScaAuthorisationAndCancelPayment(spiContextDataProvider.provideWithPsuIdData(psuData), spiScaConfirmation, payment, spiAspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}]. PIS_CANCELLATION_EMBEDDED_SCAMETHODSELECTED stage. Verify SCA authorisation and cancel payment has failed. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS), paymentId, authorisationId, psuData);
        }

        updatePaymentAfterSpiService.updatePaymentStatus(paymentId, TransactionStatus.CANC);

        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(FINALISED, paymentId, authorisationId, psuData);
    }

    @Override
    public AuthorisationProcessorResponse doScaStarted(AuthorisationProcessorRequest authorisationProcessorRequest) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERROR_MESSAGE);
    }

    @Override
    public AuthorisationProcessorResponse doScaFinalised(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(FINALISED, request.getBusinessObjectId(), request.getAuthorisationId(), request.getPsuData());
    }

    @Override
    public AuthorisationProcessorResponse doScaFailed(AuthorisationProcessorRequest authorisationProcessorRequest) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERROR_MESSAGE);
    }

    @Override
    public AuthorisationProcessorResponse doScaExempted(AuthorisationProcessorRequest authorisationProcessorRequest) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERROR_MESSAGE);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse applyIdentification(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        String paymentId = request.getPaymentId();
        String authorisationId = request.getAuthorisationId();
        PsuIdData psuData = request.getPsuData();

        if (!isPsuExist(psuData)) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NO_PSU))
                                          .build();
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. Apply identification when update payment PSU data has failed. No PSU data available in request.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        if (!isPsuDataCorrect(paymentId, psuData)) {
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. Apply Identification when update payment PSU data has failed. PSU credentials invalid.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId());
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_401)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.UNAUTHORIZED_NO_PSU))
                                          .build();
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(PSUIDENTIFIED, paymentId, authorisationId, psuData);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse applyAuthorisation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, GetPisAuthorisationResponse pisAuthorisationResponse) {
        PsuIdData psuData = extractPsuIdData(request, pisAuthorisationResponse);
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();
        PaymentType paymentType = pisAuthorisationResponse.getPaymentType();
        String paymentProduct = pisAuthorisationResponse.getPaymentProduct();
        SpiPayment payment = xs2aToSpiPaymentMapper.mapToSpiPayment(pisAuthorisationResponse, paymentType, paymentProduct);

        if (pisAuthorisationResponse.getChosenScaApproach() == ScaApproach.DECOUPLED) {
            request.setPsuData(psuData);
        }

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(paymentId);

        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiPsuData spiPsuData = xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuData);

        SpiResponse<SpiPsuAuthorisationResponse> authPsuResponse = paymentCancellationSpi.authorisePsu(spiContextData, spiPsuData, request.getPassword(), payment, spiAspspConsentDataProvider);

        if (authPsuResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(authPsuResponse, ServiceType.PIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. Authorise PSU when apply authorisation has failed. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId(), errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        SpiPsuAuthorisationResponse psuAuthorisationResponse = authPsuResponse.getPayload();

        if (psuAuthorisationResponse.getSpiAuthorisationStatus() == SpiAuthorisationStatus.FAILURE) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_401)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID))
                                          .build();
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. PSU authorisation failed due to incorrect credentials. Error msg: [{}].",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId(), errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        if (psuAuthorisationResponse.isScaExempted() && paymentType != PaymentType.PERIODIC) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. SCA was exempted for the payment after AuthorisationSpi#authorisePsu.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId());
            return cancelPaymentWithoutSca(request, psuData, payment, spiContextData, EXEMPTED);
        }

        if (pisAuthorisationResponse.getChosenScaApproach() == ScaApproach.DECOUPLED) {
            return pisCommonDecoupledService.proceedDecoupledCancellation(request, payment);
        }

        SpiResponse<SpiAvailableScaMethodsResponse> availableScaMethodsResponse = paymentCancellationSpi.requestAvailableScaMethods(spiContextData, payment, spiAspspConsentDataProvider);

        if (availableScaMethodsResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(availableScaMethodsResponse, ServiceType.PIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. Request available SCA methods has failed. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId(), errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        SpiAvailableScaMethodsResponse availableScaMethods = availableScaMethodsResponse.getPayload();

        if (availableScaMethods.isScaExempted() && paymentType != PaymentType.PERIODIC) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. SCA was exempted for the payment after AuthorisationSpi#requestAvailableScaMethods.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId());
            return cancelPaymentWithoutSca(request, psuData, payment, spiContextData, EXEMPTED);
        }

        List<SpiAuthenticationObject> spiScaMethods = availableScaMethods.getAvailableScaMethods();

        if (CollectionUtils.isEmpty(spiScaMethods)) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. Available SCA methods is empty.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId());

            SpiResponse<SpiResponse.VoidResponse> executePaymentResponse = paymentCancellationSpi.cancelPaymentWithoutSca(spiContextData, payment, spiAspspConsentDataProvider);

            if (executePaymentResponse.hasError()) {
                ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(executePaymentResponse, ServiceType.PIS);
                log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. Cancel payment without SCA has failed. Error msg: [{}]",
                         requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId(), errorHolder);
                return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
            }

            updatePaymentAfterSpiService.updatePaymentStatus(paymentId, TransactionStatus.CANC);

            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(FINALISED, paymentId, authorisationId, psuData);

        } else if (isSingleScaMethod(spiScaMethods)) {
            xs2aPisCommonPaymentService.saveAuthenticationMethods(authorisationId, spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(spiScaMethods));
            SpiAuthenticationObject chosenMethod = spiScaMethods.get(0);

            if (chosenMethod.isDecoupled()) {
                xs2aPisCommonPaymentService.updateScaApproach(authorisationId, ScaApproach.DECOUPLED);
                return pisCommonDecoupledService.proceedDecoupledCancellation(request, payment, chosenMethod.getAuthenticationMethodId());
            }

            return proceedSingleScaEmbeddedApproach(payment, chosenMethod, spiContextData, spiAspspConsentDataProvider, request, psuData, pisAuthorisationResponse);

        } else if (isMultipleScaMethods(spiScaMethods)) {
            xs2aPisCommonPaymentService.saveAuthenticationMethods(authorisationId, spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(spiScaMethods));
            Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aUpdatePisCommonPaymentPsuDataResponse(PSUAUTHENTICATED, paymentId, authorisationId, psuData);
            response.setAvailableScaMethods(spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(spiScaMethods));
            return response;
        }

        log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. Apply authorisation when update payment PSU data set SCA status failed.",
                 requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId);
        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(FAILED, paymentId, authorisationId, psuData);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse proceedSingleScaEmbeddedApproach(SpiPayment payment,
                                                                                       SpiAuthenticationObject chosenMethod,
                                                                                       SpiContextData contextData,
                                                                                       SpiAspspConsentDataProvider spiAspspConsentDataProvider,
                                                                                       Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                                       PsuIdData psuData,
                                                                                       GetPisAuthorisationResponse pisAuthorisationResponse) {
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();

        SpiResponse<SpiAuthorizationCodeResult> authCodeResponse = paymentCancellationSpi.requestAuthorisationCode(contextData, chosenMethod.getAuthenticationMethodId(), payment, spiAspspConsentDataProvider);

        if (authCodeResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(authCodeResponse, ServiceType.PIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. Proceed single SCA embedded approach when performs authorisation has failed. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId(), errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        SpiAuthorizationCodeResult authorizationCodeResult = authCodeResponse.getPayload();

        if (authorizationCodeResult.isScaExempted() && payment.getPaymentType() != PaymentType.PERIODIC) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. SCA was exempted for the payment after AuthorisationSpi#requestAuthorisationCode.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId());
            return cancelPaymentWithoutSca(request, psuData, payment, contextData, EXEMPTED);
        }

        ChallengeData challengeData = mapToChallengeData(authCodeResponse.getPayload());

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aUpdatePisCommonPaymentPsuDataResponse(SCAMETHODSELECTED, paymentId, authorisationId, psuData);
        response.setChosenScaMethod(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(chosenMethod));
        response.setChallengeData(challengeData);
        return response;
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse proceedEmbeddedApproach(Xs2aUpdatePisCommonPaymentPsuDataRequest request, SpiPayment payment, GetPisAuthorisationResponse pisAuthorisationResponse) {
        String authenticationMethodId = request.getAuthenticationMethodId();
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();
        PsuIdData psuData = extractPsuIdData(request, pisAuthorisationResponse);
        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(paymentId);

        SpiResponse<SpiAuthorizationCodeResult> spiResponse = paymentCancellationSpi.requestAuthorisationCode(spiContextData, authenticationMethodId, payment, spiAspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_PSUAUTHENTICATED stage. Proceed embedded approach when performs authorisation depending on selected SCA method has failed. Error msg: {}.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId(), errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        SpiAuthorizationCodeResult authorizationCodeResult = spiResponse.getPayload();

        if (authorizationCodeResult.isScaExempted() && payment.getPaymentType() != PaymentType.PERIODIC) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_PSUAUTHENTICATED stage. SCA was exempted for the payment after AuthorisationSpi#requestAuthorisationCode.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId());
            return cancelPaymentWithoutSca(request, psuData, payment, spiContextData, EXEMPTED);
        }

        if (authorizationCodeResult.isEmpty()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_PSUAUTHENTICATED stage. Proceed embedded approach when update payment PSU data has failed. SCA_METHOD_UNKNOWN",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId());
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400).tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_METHOD_UNKNOWN))
                                          .build();
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        SpiAuthenticationObject spiAuthenticationObject = authorizationCodeResult.getSelectedScaMethod();
        ChallengeData challengeData = mapToChallengeData(authorizationCodeResult);

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aUpdatePisCommonPaymentPsuDataResponse(SCAMETHODSELECTED, paymentId, authorisationId, psuData);
        response.setChosenScaMethod(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(spiAuthenticationObject));
        response.setChallengeData(challengeData);
        response.setPsuData(psuData);
        return response;
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse cancelPaymentWithoutSca(Xs2aUpdatePisCommonPaymentPsuDataRequest request, PsuIdData psuData, SpiPayment payment, SpiContextData contextData, ScaStatus resultScaStatus) {
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();

        final SpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(paymentId);

        paymentCancellationSpi.cancelPaymentWithoutSca(contextData, payment, aspspConsentDataProvider);
        updatePaymentAfterSpiService.updatePaymentStatus(paymentId, TransactionStatus.CANC);
        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(resultScaStatus, paymentId, authorisationId, psuData);
    }

    private boolean isSingleScaMethod(List<SpiAuthenticationObject> spiScaMethods) {
        return spiScaMethods.size() == 1;
    }

    private boolean isMultipleScaMethods(List<SpiAuthenticationObject> spiScaMethods) {
        return spiScaMethods.size() > 1;
    }

    private boolean isPsuDataCorrect(String paymentId, PsuIdData psuData) {
        List<PsuIdData> psuIdDataList = pisPsuDataService.getPsuDataByPaymentId(paymentId);

        return psuIdDataList.stream()
                   .anyMatch(psu -> psu.contentEquals(psuData));
    }

    private PisScaAuthorisationService getService(ScaApproach scaApproach) {
        return services.stream().filter(s -> s.getScaApproachServiceType() == scaApproach).findFirst()
                   .orElseThrow(() -> new IllegalArgumentException("Pis cancellation authorisation service was not found for approach " + scaApproach));
    }

    private PsuIdData extractPsuIdData(UpdateAuthorisationRequest request,
                                       GetPisAuthorisationResponse authorisationResponse) {
        PsuIdData psuDataInRequest = request.getPsuData();
        return isPsuExist(psuDataInRequest) ? psuDataInRequest : authorisationResponse.getPsuIdData();
    }

    private ChallengeData mapToChallengeData(SpiAuthorizationCodeResult authorizationCodeResult) {
        if (authorizationCodeResult != null && !authorizationCodeResult.isEmpty()) {
            return authorizationCodeResult.getChallengeData();
        }
        return null;
    }

    private boolean isDecoupledApproach(String authorisationId, String authenticationMethodId) {
        return xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }
}
