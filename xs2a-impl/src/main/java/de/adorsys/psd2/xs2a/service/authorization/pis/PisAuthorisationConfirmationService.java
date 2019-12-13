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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.consent.api.service.PisAuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.spi.payment.SpiPaymentServiceResolver;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiConfirmationCode;
import de.adorsys.psd2.xs2a.spi.service.PaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.FINALISED;

@Slf4j
@Component
@RequiredArgsConstructor
public class PisAuthorisationConfirmationService {

    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;
    private final RequestProviderService requestProviderService;
    private final Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;
    private final Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    private final PisAuthorisationServiceEncrypted pisAuthorisationServiceEncrypted;
    private final SpiPaymentServiceResolver spiPaymentServiceResolver;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;

    /**
     * Checks authorisation confirmation data. Has two possible flows:
     * - data is checked at XS2A side, we compare the data from DB with the incoming data;
     * - data is transferred to SPI level and checking should be implemented at ASPSP side.
     *
     * @param request                     {@link Xs2aUpdatePisCommonPaymentPsuDataRequest} with all payment information.
     * @param getPisAuthorisationResponse (@link GetPisAuthorisationResponse) object from the DB.
     * @return {@link Xs2aUpdatePisCommonPaymentPsuDataResponse} with new authorisation status.
     */
    public Xs2aUpdatePisCommonPaymentPsuDataResponse processAuthorisationConfirmation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, GetPisAuthorisationResponse getPisAuthorisationResponse) {

        if (aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()) {
            return checkAuthorisationConfirmationXs2a(request, getPisAuthorisationResponse);
        }

        return checkAuthorisationConfirmationOnSpi(request, getPisAuthorisationResponse);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse checkAuthorisationConfirmationXs2a(Xs2aUpdatePisCommonPaymentPsuDataRequest request, GetPisAuthorisationResponse getPisAuthorisationResponse) {

        if (!StringUtils.equals(request.getConfirmationCode(), getPisAuthorisationResponse.getScaAuthenticationData())) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_SCA_STATUS))
                                          .build();
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}]. Updating PIS authorisation PSU Data has failed: confirmation code is wrong.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), request.getPaymentId(), request.getAuthorisationId());

            return createResponse(null, errorHolder, request);
        }

        return createResponse(FINALISED, null, request);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse checkAuthorisationConfirmationOnSpi(Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                                          GetPisAuthorisationResponse getPisAuthorisationResponse) {
        PaymentSpi paymentSpi = spiPaymentServiceResolver.getPaymentService(getPisAuthorisationResponse, getPisAuthorisationResponse.getPaymentType());

        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(request.getPsuData());
        SpiPayment payment = xs2aToSpiPaymentMapper.mapToSpiPayment(getPisAuthorisationResponse, request.getPaymentService(), request.getPaymentProduct());
        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getPaymentId());
        SpiConfirmationCode spiConfirmationCode = new SpiConfirmationCode(request.getConfirmationCode());

        ScaStatus aspspScaStatus = (ScaStatus) paymentSpi.checkConfirmationCode(contextData, spiConfirmationCode, payment, aspspConsentDataProvider).getPayload();

        return createResponse(aspspScaStatus, null, request);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse createResponse(ScaStatus scaStatus, ErrorHolder errorHolder, Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        Xs2aUpdatePisCommonPaymentPsuDataResponse response =
            errorHolder == null
                ? new Xs2aUpdatePisCommonPaymentPsuDataResponse(scaStatus, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData())
                : new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());

        UpdatePisCommonPaymentPsuDataRequest updatePaymentRequest = pisCommonPaymentMapper.mapToCmsUpdateCommonPaymentPsuDataReq(response);
        pisAuthorisationServiceEncrypted.updatePisAuthorisation(updatePaymentRequest.getAuthorizationId(), updatePaymentRequest);

        return response;
    }

}
