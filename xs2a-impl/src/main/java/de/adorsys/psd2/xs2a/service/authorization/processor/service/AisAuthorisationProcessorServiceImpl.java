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

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.CommonDecoupledAisService;
import de.adorsys.psd2.xs2a.service.authorization.processor.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD")
public class AisAuthorisationProcessorServiceImpl extends BaseAuthorisationProcessorService {
    private static final String UNSUPPORTED_ERROR_MESSAGE = "Current SCA status is not supported";

    private final List<AisAuthorizationService> services;
    private final Xs2aAisConsentService aisConsentService;
    private final RequestProviderService requestProviderService;
    private final AisConsentSpi aisConsentSpi;
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final SpiErrorMapper spiErrorMapper;
    private final CommonDecoupledAisService commonDecoupledAisService;
    private final SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;
    private final AisScaAuthorisationService aisScaAuthorisationService;
    private final Xs2aToSpiPsuDataMapper psuDataMapper;

    @Override
    public void updateAuthorisation(AuthorisationProcessorRequest request, AuthorisationProcessorResponse response) {
        AisAuthorizationService authorizationService = getService(request.getScaApproach());
        authorizationService.updateConsentPsuData(request.getUpdateAuthorisationRequest(), response);
    }

    @Override
    public AuthorisationProcessorResponse doScaReceived(AuthorisationProcessorRequest authorisationProcessorRequest) {
        return doScaPsuIdentified(authorisationProcessorRequest);
    }

    @Override
    public AuthorisationProcessorResponse doScaPsuIdentified(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        return request.isUpdatePsuIdentification()
                   ? applyIdentification(authorisationProcessorRequest)
                   : applyAuthorisation(authorisationProcessorRequest);
    }

    @Override
    public AuthorisationProcessorResponse doScaPsuAuthenticated(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String consentId = request.getBusinessObjectId();
        String authorisationId = request.getAuthorisationId();
        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);
        if (!accountConsentOptional.isPresent()) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                          .build();
            writeLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, "Apply authorisation when update consent PSU data has failed. Consent not found by id.");
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
        }

        PsuIdData psuData = extractPsuIdData(request, (AccountConsentAuthorization) authorisationProcessorRequest.getAuthorisation());
        AccountConsent accountConsent = accountConsentOptional.get();

        SpiAccountConsent spiAccountConsent = aisConsentMapper.mapToSpiAccountConsent(accountConsent);

        String authenticationMethodId = request.getAuthenticationMethodId();
        if (isDecoupledApproach(request.getAuthorisationId(), authenticationMethodId)) {
            aisConsentService.updateScaApproach(request.getAuthorisationId(), ScaApproach.DECOUPLED);
            return commonDecoupledAisService.proceedDecoupledApproach(request.getBusinessObjectId(), request.getAuthorisationId(), spiAccountConsent, authenticationMethodId, psuData);
        }

        return proceedEmbeddedApproach(authorisationProcessorRequest, authenticationMethodId, spiAccountConsent, psuData);
    }

    @Override
    public AuthorisationProcessorResponse doScaMethodSelected(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String consentId = request.getBusinessObjectId();
        String authorisationId = request.getAuthorisationId();
        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (!accountConsentOptional.isPresent()) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                          .build();
            writeLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, "Apply authorisation when update consent PSU data has failed. Consent not found by id.");
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
        }
        AccountConsent accountConsent = accountConsentOptional.get();

        PsuIdData psuData = extractPsuIdData(request, (AccountConsentAuthorization) authorisationProcessorRequest.getAuthorisation());

        SpiResponse<SpiVerifyScaAuthorisationResponse> spiResponse = aisConsentSpi.verifyScaAuthorisation(spiContextDataProvider.provideWithPsuIdData(psuData),
                                                                                                          aisConsentMapper.mapToSpiScaConfirmation(request, psuData),
                                                                                                          aisConsentMapper.mapToSpiAccountConsent(accountConsent),
                                                                                                          aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
            writeLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, "Verify SCA authorisation failed when update PSU data.");
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
        }

        ConsentStatus responseConsentStatus = spiResponse.getPayload().getConsentStatus();

        if (ConsentStatus.PARTIALLY_AUTHORISED == responseConsentStatus && !accountConsent.isMultilevelScaRequired()) {
            aisConsentService.updateMultilevelScaRequired(consentId, true);
        }

        if (accountConsent.getConsentStatus() != responseConsentStatus) {
            aisConsentService.updateConsentStatus(consentId, responseConsentStatus);
        }
        aisConsentService.findAndTerminateOldConsentsByNewConsentId(consentId);

        return new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, consentId, request.getAuthorisationId());
    }

    @Override
    public AuthorisationProcessorResponse doScaStarted(AuthorisationProcessorRequest authorisationProcessorRequest) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERROR_MESSAGE);
    }

    @Override
    public AuthorisationProcessorResponse doScaFinalised(AuthorisationProcessorRequest authorisationProcessorRequest) {
        return statusResponse(ScaStatus.FINALISED, authorisationProcessorRequest);
    }

    @Override
    public AuthorisationProcessorResponse doScaFailed(AuthorisationProcessorRequest authorisationProcessorRequest) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERROR_MESSAGE);
    }

    @Override
    public AuthorisationProcessorResponse doScaExempted(AuthorisationProcessorRequest authorisationProcessorRequest) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERROR_MESSAGE);
    }

    private UpdateConsentPsuDataResponse proceedEmbeddedApproach(AuthorisationProcessorRequest authorisationProcessorRequest, String authenticationMethodId, SpiAccountConsent spiAccountConsent, PsuIdData psuData) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        SpiResponse<SpiAuthorizationCodeResult> spiResponse = aisConsentSpi.requestAuthorisationCode(spiContextDataProvider.provideWithPsuIdData(psuData), authenticationMethodId, spiAccountConsent, aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getBusinessObjectId()));

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
            writeLog(authorisationProcessorRequest, psuData, errorHolder, "Proceed embedded approach when performs authorisation depending on selected SCA method has failed.");
            return new UpdateConsentPsuDataResponse(errorHolder, request.getBusinessObjectId(), request.getAuthorisationId());
        }

        SpiAuthorizationCodeResult authorizationCodeResult = spiResponse.getPayload();

        SpiAuthenticationObject chosenScaMethod = authorizationCodeResult.getSelectedScaMethod();
        ChallengeData challengeData = authorizationCodeResult.getChallengeData();

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.SCAMETHODSELECTED, request.getBusinessObjectId(), request.getAuthorisationId());
        response.setChosenScaMethod(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(chosenScaMethod));
        response.setChallengeData(challengeData);
        return response;
    }

    private AisAuthorizationService getService(ScaApproach scaApproach) {
        return services.stream().filter(s -> s.getScaApproachServiceType() == scaApproach).findFirst()
                   .orElseThrow(() -> new IllegalArgumentException("Ais authorisation service was not found for approach " + scaApproach));
    }

    private PsuIdData extractPsuIdData(UpdateAuthorisationRequest request, AccountConsentAuthorization authorisationResponse) {
        PsuIdData psuDataInRequest = request.getPsuData();
        return isPsuExist(psuDataInRequest) ? psuDataInRequest : authorisationResponse.getPsuIdData();
    }

    private UpdateConsentPsuDataResponse applyAuthorisation(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        AccountConsentAuthorization authorisationResponse = (AccountConsentAuthorization) authorisationProcessorRequest.getAuthorisation();
        String consentId = request.getBusinessObjectId();
        String authorisationId = request.getAuthorisationId();
        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (!accountConsentOptional.isPresent()) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                          .build();
            writeLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, "Apply authorisation when update consent PSU data has failed. Consent not found by id.");
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
        }

        AccountConsent accountConsent = accountConsentOptional.get();
        PsuIdData psuData = extractPsuIdData(request, authorisationResponse);

        SpiAccountConsent spiAccountConsent = aisConsentMapper.mapToSpiAccountConsent(accountConsent);
        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiPsuData spiPsuData = psuDataMapper.mapToSpiPsuData(psuData);
        SpiResponse<SpiPsuAuthorisationResponse> authorisationStatusSpiResponse = aisConsentSpi.authorisePsu(spiContextData,
                                                                                                             spiPsuData,
                                                                                                             request.getPassword(),
                                                                                                             spiAccountConsent,
                                                                                                             aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        if (authorisationStatusSpiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(authorisationStatusSpiResponse, ServiceType.AIS);
            writeLog(authorisationProcessorRequest, psuData, errorHolder, "Authorise PSU when apply authorisation has failed.");
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
        }

        if (authorisationStatusSpiResponse.getPayload().getSpiAuthorisationStatus() == SpiAuthorisationStatus.FAILURE) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_401)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID))
                                          .build();
            writeLog(authorisationProcessorRequest, psuData, errorHolder, "Authorise PSU when apply authorisation has failed. PSU credentials invalid.");
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
        }

        if (aisScaAuthorisationService.isOneFactorAuthorisation(accountConsent)) {
            aisConsentService.updateConsentStatus(consentId, ConsentStatus.VALID);

            return new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, consentId, authorisationId);
        }

        if (authorisationResponse.getChosenScaApproach() == ScaApproach.DECOUPLED) {
            return commonDecoupledAisService.proceedDecoupledApproach(consentId, authorisationId, spiAccountConsent, psuData);
        }

        SpiResponse<SpiAvailableScaMethodsResponse> spiResponse = aisConsentSpi.requestAvailableScaMethods(spiContextDataProvider.provideWithPsuIdData(psuData), spiAccountConsent, aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
            writeLog(authorisationProcessorRequest, psuData, errorHolder, "Request available SCA methods when apply authorisation has failed.");
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
        }

        List<SpiAuthenticationObject> availableScaMethods = spiResponse.getPayload().getAvailableScaMethods();
        if (CollectionUtils.isNotEmpty(availableScaMethods)) {
            aisConsentService.saveAuthenticationMethods(authorisationId, spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(availableScaMethods));

            if (availableScaMethods.size() > 1) {
                return createResponseForMultipleAvailableMethods(availableScaMethods, authorisationId, consentId);
            } else {
                return createResponseForOneAvailableMethod(authorisationProcessorRequest, spiAccountConsent, availableScaMethods.get(0), psuData);
            }
        } else {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_METHOD_UNKNOWN))
                                          .build();
            writeLog(authorisationProcessorRequest, psuData, errorHolder, "Apply authorisation has failed. Consent rejected because no available SCA methods.");
            aisConsentService.updateConsentStatus(consentId, ConsentStatus.REJECTED);
            UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
            aisConsentService.updateConsentAuthorization(aisConsentMapper.mapToSpiUpdateConsentPsuDataReq(request, response));
            return response;
        }
    }

    private UpdateConsentPsuDataResponse createResponseForMultipleAvailableMethods(List<SpiAuthenticationObject> availableScaMethods, String authorisationId, String consentId) {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.PSUAUTHENTICATED, consentId, authorisationId);
        response.setAvailableScaMethods(spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(availableScaMethods));
        return response;
    }

    private UpdateConsentPsuDataResponse createResponseForOneAvailableMethod(AuthorisationProcessorRequest authorisationProcessorRequest, SpiAccountConsent spiAccountConsent, SpiAuthenticationObject scaMethod, PsuIdData psuData) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        if (scaMethod.isDecoupled()) {
            aisConsentService.updateScaApproach(request.getAuthorisationId(), ScaApproach.DECOUPLED);
            return commonDecoupledAisService.proceedDecoupledApproach(request.getBusinessObjectId(), request.getAuthorisationId(), spiAccountConsent, scaMethod.getAuthenticationMethodId(), psuData);
        }

        return proceedEmbeddedApproach(authorisationProcessorRequest, scaMethod.getAuthenticationMethodId(), spiAccountConsent, psuData);
    }

    private UpdateConsentPsuDataResponse applyIdentification(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        if (!isPsuExist(request.getPsuData())) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NO_PSU))
                                          .build();
            writeLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, "Apply identification when update consent PSU data has failed. No PSU data available in request.");
            return new UpdateConsentPsuDataResponse(errorHolder, request.getBusinessObjectId(), request.getAuthorisationId());
        }

        return new UpdateConsentPsuDataResponse(ScaStatus.PSUIDENTIFIED, request.getBusinessObjectId(), request.getAuthorisationId());
    }

    private void writeLog(AuthorisationProcessorRequest request, PsuIdData psuData, ErrorHolder errorHolder, String message) {
        String messageToLog = String.format("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}], Authorisation-ID [{}], PSU-ID [{}], SCA Approach [{}]. %s Error msg: [{}]", message);
        log.info(messageToLog,
                 requestProviderService.getInternalRequestId(),
                 requestProviderService.getRequestId(),
                 request.getUpdateAuthorisationRequest().getBusinessObjectId(),
                 request.getUpdateAuthorisationRequest().getAuthorisationId(),
                 psuData != null ? psuData.getPsuId() : "-",
                 request.getScaApproach(),
                 errorHolder);
    }

    private UpdateConsentPsuDataResponse statusResponse(ScaStatus scaStatus, AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        return new UpdateConsentPsuDataResponse(scaStatus, request.getBusinessObjectId(), request.getAuthorisationId());
    }

    private boolean isDecoupledApproach(String authorisationId, String authenticationMethodId) {
        return aisConsentService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }
}
