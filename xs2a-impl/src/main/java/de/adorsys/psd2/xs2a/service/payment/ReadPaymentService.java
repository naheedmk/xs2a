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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInformationResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class ReadPaymentService<T> {

    protected SpiContextDataProvider spiContextDataProvider;
    protected SpiPaymentFactory spiPaymentFactory;

    private SpiErrorMapper spiErrorMapper;
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private RequestProviderService requestProviderService;
    private Xs2aUpdatePaymentAfterSpiService updatePaymentStatusAfterSpiService;

    public ReadPaymentService(SpiErrorMapper spiErrorMapper, SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory, RequestProviderService requestProviderService, Xs2aUpdatePaymentAfterSpiService updatePaymentStatusAfterSpiService, SpiContextDataProvider spiContextDataProvider, SpiPaymentFactory spiPaymentFactory) {
        this.spiContextDataProvider = spiContextDataProvider;
        this.spiPaymentFactory = spiPaymentFactory;
        this.spiErrorMapper = spiErrorMapper;
        this.aspspConsentDataProviderFactory = aspspConsentDataProviderFactory;
        this.requestProviderService = requestProviderService;
        this.updatePaymentStatusAfterSpiService = updatePaymentStatusAfterSpiService;
    }

    public T getPayment(List<PisPayment> pisPayments, String paymentProduct, PsuIdData psuData, @NotNull String encryptedPaymentId) {
        Optional spiPaymentOptional = createSpiPayment(pisPayments, paymentProduct);

        if (!spiPaymentOptional.isPresent()) {
            return (T) new PaymentInformationResponse(
                ErrorHolder.builder(ErrorType.PIS_404)
                    .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404, "Payment not found"))
                    .build());
        }

        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuData);

        SpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(encryptedPaymentId);

        SpiResponse spiResponse = getSpiPaymentById(spiContextData, spiPaymentOptional.get(), aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}]. Read payment failed. Can't get payment by ID at SPI level. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), ((SpiPayment) spiPaymentOptional.get()).getPaymentId(), errorHolder);
            return (T) new PaymentInformationResponse(errorHolder);
        }

        Object xs2aPayment = getXs2aPayment(spiResponse);

        TransactionStatus paymentStatus = ((SpiPayment) spiResponse.getPayload()).getPaymentStatus();

        if (!updatePaymentStatusAfterSpiService.updatePaymentStatus(encryptedPaymentId, paymentStatus)) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Internal payment ID: [{}], Transaction status: [{}]. Update of a payment status in the CMS has failed.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), ((SpiPayment) spiResponse.getPayload()).getPaymentId(), paymentStatus);
        }

        return (T) new PaymentInformationResponse(xs2aPayment);

    }

    protected abstract Optional createSpiPayment(List<PisPayment> pisPayments, String paymentProduct);

    protected abstract SpiResponse getSpiPaymentById(SpiContextData spiContextData, Object spiPayment, SpiAspspConsentDataProvider aspspConsentDataProvider);

    protected abstract Object getXs2aPayment(SpiResponse spiResponse);
}
