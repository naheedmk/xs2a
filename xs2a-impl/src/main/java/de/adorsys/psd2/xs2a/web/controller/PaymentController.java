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

package de.adorsys.psd2.xs2a.web.controller;

import de.adorsys.psd2.api.PaymentApi;
import de.adorsys.psd2.model.PaymentInitationRequestResponse201;
import de.adorsys.psd2.model.PaymentInitiationCancelResponse202;
import de.adorsys.psd2.model.PeriodicPaymentInitiationXmlPart2StandingorderTypeJson;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPaymentCancellationAuthorisationSubResource;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.PaymentAuthorisationService;
import de.adorsys.psd2.xs2a.service.PaymentCancellationAuthorisationService;
import de.adorsys.psd2.xs2a.service.PaymentService;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.web.header.PaymentCancellationHeadersBuilder;
import de.adorsys.psd2.xs2a.web.header.PaymentInitiationHeadersBuilder;
import de.adorsys.psd2.xs2a.web.header.ResponseHeaders;
import de.adorsys.psd2.xs2a.web.mapper.AuthorisationMapper;
import de.adorsys.psd2.xs2a.web.mapper.ConsentModelMapper;
import de.adorsys.psd2.xs2a.web.mapper.PaymentModelMapperPsd2;
import de.adorsys.psd2.xs2a.web.mapper.PaymentModelMapperXs2a;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_404;


@SuppressWarnings("unchecked") // This class implements autogenerated interface without proper return values generated
@RestController
@AllArgsConstructor
@Api(value = "v1", description = "Provides access to the payment initiation", tags = {"Payment Initiation Service (PIS)"})
public class PaymentController implements PaymentApi {
    private final PaymentService xs2aPaymentService;
    private final ResponseMapper responseMapper;
    private final ResponseErrorMapper responseErrorMapper;
    private final PaymentModelMapperPsd2 paymentModelMapperPsd2;
    private final PaymentModelMapperXs2a paymentModelMapperXs2a;
    private final ConsentModelMapper consentModelMapper;
    private final PaymentAuthorisationService paymentAuthorisationService;
    private final PaymentCancellationAuthorisationService paymentCancellationAuthorisationService;
    private final AuthorisationMapper authorisationMapper;
    private final PaymentInitiationHeadersBuilder paymentInitiationHeadersBuilder;
    private final PaymentCancellationHeadersBuilder paymentCancellationHeadersBuilder;

    @Override
    public ResponseEntity getPaymentInitiationStatus(String paymentService, String paymentProduct,
                                                     String paymentId, UUID xRequestID, String digest,
                                                     String signature, byte[] tpPSignatureCertificate,
                                                     String psUIPAddress, String psUIPPort, String psUAccept,
                                                     String psUAcceptCharset, String psUAcceptEncoding,
                                                     String psUAcceptLanguage, String psUUserAgent,
                                                     String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {

        ResponseObject<TransactionStatus> serviceResponse = PaymentType.getByValue(paymentService)
                                                                .map(pt -> xs2aPaymentService.getPaymentStatusById(pt, paymentProduct, paymentId))
                                                                .orElseGet(ResponseObject.<TransactionStatus>builder()
                                                                               .fail(ErrorType.PIS_404, TppMessageInformation.of(RESOURCE_UNKNOWN_404))::build);
        return serviceResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(serviceResponse.getError())
                   : responseMapper.ok(serviceResponse, PaymentModelMapperPsd2::mapToStatusResponse);
    }

    @Override
    public ResponseEntity getPaymentInformation(String paymentService, String paymentProduct, String paymentId, UUID xRequestID, String digest,
                                                String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort,
                                                String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage,
                                                String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {

        ResponseObject serviceResponse = PaymentType.getByValue(paymentService)
                                             .map(pt -> xs2aPaymentService.getPaymentById(pt, paymentProduct, paymentId))
                                             .orElseGet(ResponseObject.builder()
                                                            .fail(ErrorType.PIS_404, TppMessageInformation.of(RESOURCE_UNKNOWN_404))::build);

        //TODO Don't provide "creditorAddress" field in "getPaymentInformation" response if it was absent in "initiatePayment" request https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/869
        return serviceResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(serviceResponse.getError())
                   : responseMapper.ok(ResponseObject.builder().body(paymentModelMapperPsd2.mapToGetPaymentResponse(serviceResponse.getBody(), PaymentType.getByValue(paymentService).get(),
                                                                                                                    paymentProduct)).build());
    }

    //Method for JSON format payments
    @Override
    public ResponseEntity initiatePayment(@Valid Object body, UUID xRequestID, String psUIPAddress, String paymentService, String paymentProduct,
                                          String digest, String signature, byte[] tpPSignatureCertificate, String PSU_ID, String psUIDType,
                                          String psUCorporateID, String psUCorporateIDType, String consentID, Boolean tpPRedirectPreferred,
                                          String tpPRedirectURI, String tpPNokRedirectURI, Boolean tpPExplicitAuthorisationPreferred,
                                          String tpPRejectionNoFundsPreferred, String tpPNotificationURI, String tpPNotificationContentPreferred,
                                          String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                          String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod,
                                          UUID psUDeviceID, String psUGeoLocation) {

        // As this method is mapped to '/v1/{payment-service}/{payment-product}' path, we need to check payment-service value to be compliant with spec
        if (!PaymentType.getByValue(paymentService).isPresent()) {
            ResponseObject<TransactionStatus> responseObject = ResponseObject.<TransactionStatus>builder()
                                                                   .fail(ErrorType.PIS_404, TppMessageInformation.of(RESOURCE_UNKNOWN_404)).build();
            return responseErrorMapper.generateErrorResponse(responseObject.getError());
        }

        PsuIdData psuData = new PsuIdData(PSU_ID, psUIDType, psUCorporateID, psUCorporateIDType);
        PaymentInitiationParameters paymentInitiationParameters = paymentModelMapperPsd2.mapToPaymentRequestParameters(paymentProduct, paymentService, tpPSignatureCertificate, tpPRedirectURI, tpPNokRedirectURI, BooleanUtils.isTrue(tpPExplicitAuthorisationPreferred), psuData);
        ResponseObject<PaymentInitiationResponse> serviceResponse =
            xs2aPaymentService.createPayment(paymentModelMapperXs2a.mapToXs2aPayment(body, paymentInitiationParameters), paymentInitiationParameters);

        if (serviceResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError(),
                                                             paymentInitiationHeadersBuilder.buildErrorInitiatePaymentHeaders());
        }

        PaymentInitiationResponse serviceResponseBody = serviceResponse.getBody();
        ResponseHeaders responseHeaders = buildPaymentInitiationResponseHeaders(serviceResponseBody);

        return responseMapper.created(ResponseObject
                                          .builder()
                                          .body(paymentModelMapperPsd2.mapToPaymentInitiationResponse(serviceResponseBody))
                                          .build(), responseHeaders);
    }

    //Method for pain.001 payment products
    @Override
    public ResponseEntity<PaymentInitationRequestResponse201> initiatePayment(UUID xRequestID, String psUIPAddress,
                                                                              String paymentService, String paymentProduct, Object xmlSct,
                                                                              PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonStandingorderType,
                                                                              String digest, String signature, byte[] tpPSignatureCertificate,
                                                                              String PSU_ID, String psUIDType, String psUCorporateID,
                                                                              String psUCorporateIDType, String consentID,
                                                                              Boolean tpPRedirectPreferred, String tpPRedirectURI,
                                                                              String tpPNokRedirectURI, Boolean tpPExplicitAuthorisationPreferred,
                                                                              String tpPRejectionNoFundsPreferred, String tpPNotificationURI,
                                                                              String tpPNotificationContentPreferred, String psUIPPort,
                                                                              String psUAccept, String psUAcceptCharset,
                                                                              String psUAcceptEncoding, String psUAcceptLanguage,
                                                                              String psUUserAgent, String psUHttpMethod,
                                                                              UUID psUDeviceID, String psUGeoLocation) {
        // As this method is mapped to '/v1/{payment-service}/{payment-product}' path, we need to check payment-service value to be compliant with spec
        if (!PaymentType.getByValue(paymentService).isPresent()) {
            ResponseObject<TransactionStatus> responseObject = ResponseObject.<TransactionStatus>builder()
                                                                   .fail(ErrorType.PIS_404, TppMessageInformation.of(RESOURCE_UNKNOWN_404)).build();
            return responseErrorMapper.generateErrorResponse(responseObject.getError());
        }

        PsuIdData psuData = new PsuIdData(PSU_ID, psUIDType, psUCorporateID, psUCorporateIDType);
        PaymentInitiationParameters paymentInitiationParameters = paymentModelMapperPsd2.mapToPaymentRequestParameters(paymentProduct, paymentService, tpPSignatureCertificate, tpPRedirectURI, tpPNokRedirectURI, BooleanUtils.isTrue(tpPExplicitAuthorisationPreferred), psuData);
        ResponseObject<PaymentInitiationResponse> serviceResponse =
            xs2aPaymentService.createPayment(paymentModelMapperXs2a.mapToXs2aRawPayment(paymentInitiationParameters, xmlSct, jsonStandingorderType), paymentInitiationParameters);

        if (serviceResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError(),
                                                             paymentInitiationHeadersBuilder.buildErrorInitiatePaymentHeaders());
        }

        PaymentInitiationResponse serviceResponseBody = serviceResponse.getBody();
        ResponseHeaders responseHeaders = buildPaymentInitiationResponseHeaders(serviceResponseBody);

        return responseMapper.created(ResponseObject
                                          .builder()
                                          .body(paymentModelMapperPsd2.mapToPaymentInitiationResponse(serviceResponseBody))
                                          .build(), responseHeaders);
    }

    // Method for raw payment products
    @Override
    public ResponseEntity<PaymentInitationRequestResponse201> initiatePayment(String body, UUID xRequestID, String psUIPAddress, String paymentService, String paymentProduct, String digest, String signature, byte[] tpPSignatureCertificate, String PSU_ID, String psUIDType, String psUCorporateID, String psUCorporateIDType, String consentID, Boolean tpPRedirectPreferred, String tpPRedirectURI, String tpPNokRedirectURI, Boolean tpPExplicitAuthorisationPreferred, String tpPRejectionNoFundsPreferred, String tpPNotificationURI, String tpPNotificationContentPreferred, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        // As this method is mapped to '/v1/{payment-service}/{payment-product}' path, we need to check payment-service value to be compliant with spec
        if (!PaymentType.getByValue(paymentService).isPresent()) {
            ResponseObject<TransactionStatus> responseObject = ResponseObject.<TransactionStatus>builder()
                                                                   .fail(ErrorType.PIS_404, TppMessageInformation.of(RESOURCE_UNKNOWN_404)).build();
            return responseErrorMapper.generateErrorResponse(responseObject.getError());
        }

        PsuIdData psuData = new PsuIdData(PSU_ID, psUIDType, psUCorporateID, psUCorporateIDType);
        PaymentInitiationParameters paymentInitiationParameters = paymentModelMapperPsd2.mapToPaymentRequestParameters(paymentProduct, paymentService, tpPSignatureCertificate, tpPRedirectURI, tpPNokRedirectURI, BooleanUtils.isTrue(tpPExplicitAuthorisationPreferred), psuData);
        ResponseObject<PaymentInitiationResponse> serviceResponse =
            xs2aPaymentService.createPayment(body.getBytes(), paymentInitiationParameters);

        if (serviceResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError(),
                                                             paymentInitiationHeadersBuilder.buildErrorInitiatePaymentHeaders());
        }

        PaymentInitiationResponse serviceResponseBody = serviceResponse.getBody();
        ResponseHeaders responseHeaders = buildPaymentInitiationResponseHeaders(serviceResponseBody);

        return responseMapper.created(ResponseObject
                                          .builder()
                                          .body(paymentModelMapperPsd2.mapToPaymentInitiationResponse(serviceResponseBody))
                                          .build(), responseHeaders);
    }

    @Override
    public ResponseEntity cancelPayment(String paymentService, String paymentProduct, String paymentId,
                                        UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate,
                                        Boolean tpPRedirectPreferred, String tpPRedirectURI, String tpPNokRedirectURI,
                                        String psUIPAddress, String psUIPPort, String psUAccept,
                                        String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage,
                                        String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation, Boolean tppExplicitAuthorisationPreferred) {

        ResponseObject<CancelPaymentResponse> serviceResponse = PaymentType.getByValue(paymentService)
                                                                    .map(type -> xs2aPaymentService.cancelPayment(type, paymentProduct, paymentId, BooleanUtils.isTrue(tppExplicitAuthorisationPreferred)))
                                                                    .orElseGet(ResponseObject.<CancelPaymentResponse>builder()
                                                                                   .fail(ErrorType.PIS_400, TppMessageInformation.of(FORMAT_ERROR))::build);

        if (serviceResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError());
        }

        CancelPaymentResponse cancelPayment = serviceResponse.getBody();
        PaymentInitiationCancelResponse202 response = paymentModelMapperPsd2.mapToPaymentInitiationCancelResponse(cancelPayment);

        return cancelPayment.isStartAuthorisationRequired()
                   ? responseMapper.accepted(ResponseObject.builder().body(response).build())
                   : responseMapper.delete(serviceResponse);
    }

    @Override
    public ResponseEntity getPaymentCancellationScaStatus(String paymentService, String paymentProduct, String paymentId,
                                                          String cancellationId, UUID xRequestID, String digest, String signature,
                                                          byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort,
                                                          String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                                          String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod,
                                                          UUID psUDeviceID, String psUGeoLocation) {

        ResponseObject<ScaStatus> serviceResponse = paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(paymentId, cancellationId);
        return serviceResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(serviceResponse.getError())
                   : responseMapper.ok(serviceResponse, authorisationMapper::mapToScaStatusResponse);
    }

    @Override
    public ResponseEntity getPaymentInitiationAuthorisation(String paymentService, String paymentProduct, String paymentId, UUID xRequestID, String digest,
                                                            String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort,
                                                            String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage,
                                                            String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {

        ResponseObject<Xs2aAuthorisationSubResources> serviceResponse = paymentAuthorisationService.getPaymentInitiationAuthorisations(paymentId);
        return serviceResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(serviceResponse.getError())
                   : responseMapper.ok(serviceResponse, authorisationMapper::mapToAuthorisations);
    }

    @Override
    public ResponseEntity getPaymentInitiationCancellationAuthorisationInformation(String paymentService, String paymentProduct, String paymentId,
                                                                                   UUID xRequestID, String digest, String signature,
                                                                                   byte[] tpPSignatureCertificate, String psUIPAddress,
                                                                                   String psUIPPort, String psUAccept, String psUAcceptCharset,
                                                                                   String psUAcceptEncoding, String psUAcceptLanguage,
                                                                                   String psUUserAgent, String psUHttpMethod, UUID psUDeviceID,
                                                                                   String psUGeoLocation) {

        ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> serviceResponse = paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(paymentId);
        return serviceResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(serviceResponse.getError())
                   : responseMapper.ok(serviceResponse, consentModelMapper::mapToCancellations);
    }

    @Override
    public ResponseEntity getPaymentInitiationScaStatus(String paymentService, String paymentProduct, String paymentId, String authorisationId,
                                                        UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate,
                                                        String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset,
                                                        String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent,
                                                        String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {

        ResponseObject<ScaStatus> serviceResponse = paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(paymentId, authorisationId);
        return serviceResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(serviceResponse.getError())
                   : responseMapper.ok(serviceResponse, authorisationMapper::mapToScaStatusResponse);
    }

    @Override
    public ResponseEntity startPaymentAuthorisation(UUID xRequestID, String paymentService, String paymentProduct,
                                                    String paymentId, Object body, String PSU_ID, String psUIDType,
                                                    String psUCorporateID, String psUCorporateIDType, Boolean tpPRedirectPreferred,
                                                    String tpPRedirectURI, String tpPNokRedirectURI, String tpPNotificationURI,
                                                    String tpPNotificationContentPreferred, String digest, String signature,
                                                    byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort,
                                                    String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                                    String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod,
                                                    UUID psUDeviceID, String psUGeoLocation) {
        PsuIdData psuData = new PsuIdData(PSU_ID, psUIDType, psUCorporateID, psUCorporateIDType);
        Xs2aCreatePisAuthorisationRequest createRequest = authorisationMapper.mapToXs2aCreatePisAuthorisationRequest(psuData, paymentId, paymentService, paymentProduct, (Map) body);

        ResponseObject<AuthorisationResponse> createAuthResponse = paymentAuthorisationService.createPisAuthorisation(createRequest);

        if (createAuthResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(createAuthResponse.getError(),
                                                             paymentInitiationHeadersBuilder.buildErrorStartPaymentAuthorisationHeaders());
        }

        AuthorisationResponse authResponse = createAuthResponse.getBody();
        ResponseHeaders responseHeaders = paymentInitiationHeadersBuilder.buildStartPaymentAuthorisationHeaders(authResponse.getAuthorisationId());

        return responseMapper.created(ResponseObject.builder()
                                          .body(authorisationMapper.mapToPisCreateOrUpdateAuthorisationResponse(createAuthResponse))
                                          .build(), responseHeaders);
    }

    @Override
    public ResponseEntity startPaymentInitiationCancellationAuthorisation(String paymentService, String paymentProduct,
                                                                          String paymentId, UUID xRequestID, String digest,
                                                                          String signature, byte[] tpPSignatureCertificate,
                                                                          String PSU_ID, String psUIDType, String psUCorporateID,
                                                                          String psUCorporateIDType, Boolean tpPRedirectPreferred,
                                                                          String tpPRedirectURI, String tpPNokRedirectURI,
                                                                          String tpPNotificationURI, String tpPNotificationContentPreferred,
                                                                          String psUIPAddress, String psUIPPort, String psUAccept,
                                                                          String psUAcceptCharset, String psUAcceptEncoding,
                                                                          String psUAcceptLanguage, String psUUserAgent,
                                                                          String psUHttpMethod, UUID psUDeviceID,
                                                                          String psUGeoLocation) {
        PsuIdData psuData = new PsuIdData(PSU_ID, psUIDType, psUCorporateID, psUCorporateIDType);
        ResponseObject<Xs2aCreatePisCancellationAuthorisationResponse> serviceResponse = paymentCancellationAuthorisationService.createPisCancellationAuthorization(paymentId, psuData, PaymentType.getByValue(paymentService).get(), paymentProduct);

        if (serviceResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError(),
                                                             paymentCancellationHeadersBuilder.buildErrorStartPaymentCancellationAuthorisationHeaders());
        }

        Xs2aCreatePisCancellationAuthorisationResponse body = serviceResponse.getBody();
        ResponseHeaders responseHeaders = paymentCancellationHeadersBuilder.buildStartPaymentCancellationAuthorisationHeaders(body.getAuthorisationId());

        return responseMapper.created(serviceResponse, consentModelMapper::mapToStartScaProcessResponse, responseHeaders);
    }

    @Override
    public ResponseEntity updatePaymentCancellationPsuData(UUID xRequestID, String paymentService, String paymentProduct, String paymentId,
                                                           String cancellationId, Object body, String digest, String signature,
                                                           byte[] tpPSignatureCertificate, String PSU_ID, String psUIDType,
                                                           String psUCorporateID, String psUCorporateIDType, String psUIPAddress,
                                                           String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                                           String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID,
                                                           String psUGeoLocation) {

        PsuIdData psuData = new PsuIdData(PSU_ID, psUIDType, psUCorporateID, psUCorporateIDType);
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> serviceResponse = paymentCancellationAuthorisationService.updatePisCancellationPsuData(consentModelMapper.mapToPisUpdatePsuData(psuData, paymentId, cancellationId, paymentService, paymentProduct, (Map) body));

        if (serviceResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError(),
                                                             paymentCancellationHeadersBuilder.buildErrorUpdatePaymentCancellationPsuDataHeaders(cancellationId));
        }

        ResponseHeaders responseHeaders = paymentCancellationHeadersBuilder.buildUpdatePaymentCancellationPsuDataHeaders(cancellationId);

        return responseMapper.ok(serviceResponse, authorisationMapper::mapToPisUpdatePsuAuthenticationResponse, responseHeaders);
    }

    @Override
    public ResponseEntity updatePaymentPsuData(UUID xRequestID, String paymentService, String paymentProduct, String paymentId,
                                               String authorisationId, Object body, String digest, String signature, byte[] tpPSignatureCertificate,
                                               String PSU_ID, String psUIDType, String psUCorporateID, String psUCorporateIDType,
                                               String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset,
                                               String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod,
                                               UUID psUDeviceID, String psUGeoLocation) {

        PsuIdData psuData = new PsuIdData(PSU_ID, psUIDType, psUCorporateID, psUCorporateIDType);
        return updatePisAuthorisation(psuData, authorisationId, paymentService, paymentProduct, paymentId, body);
    }

    private ResponseEntity updatePisAuthorisation(PsuIdData psuData, String authorisationId, String paymentService, String paymentProduct, String paymentId, Object body) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = consentModelMapper.mapToPisUpdatePsuData(psuData, paymentId, authorisationId, paymentService, paymentProduct, (Map) body);

        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> serviceResponse = paymentAuthorisationService.updatePisCommonPaymentPsuData(request);

        if (serviceResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError(),
                                                             paymentInitiationHeadersBuilder.buildErrorUpdatePaymentInitiationPsuDataHeaders(authorisationId));
        }

        ResponseHeaders responseHeaders = paymentInitiationHeadersBuilder.buildUpdatePaymentInitiationPsuDataHeaders(authorisationId);

        return responseMapper.ok(serviceResponse, authorisationMapper::mapToPisUpdatePsuAuthenticationResponse, responseHeaders);
    }

    private ResponseHeaders buildPaymentInitiationResponseHeaders(PaymentInitiationResponse paymentInitiationResponse) {
        return paymentInitiationHeadersBuilder.buildInitiatePaymentHeaders(paymentInitiationResponse.getAuthorizationId(), paymentInitiationResponse.getLinks().getSelf());
    }
}
