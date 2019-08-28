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

package de.adorsys.psd2.xs2a.service.payment.initiation;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.CommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentInfo;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.PaymentSpi;
import org.springframework.stereotype.Service;

@Service
public class CommonPaymentInitiationService extends AbstractPaymentInitiationService<CommonPayment, SpiPaymentInfo, SpiPaymentInitiationResponse> {
    private final SpiToXs2aPaymentMapper spiToXs2aPaymentMapper;
    private final Xs2aToSpiPaymentInfo xs2aToSpiPaymentInfo;
    private final CommonPaymentSpi commonPaymentSpi;

    public CommonPaymentInitiationService(SpiContextDataProvider spiContextDataProvider, SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                          SpiErrorMapper spiErrorMapper, RequestProviderService requestProviderService,
                                          SpiToXs2aPaymentMapper spiToXs2aPaymentMapper, Xs2aToSpiPaymentInfo xs2aToSpiPaymentInfo,
                                          CommonPaymentSpi commonPaymentSpi) {
        super(spiContextDataProvider, aspspConsentDataProviderFactory, spiErrorMapper, requestProviderService);
        this.spiToXs2aPaymentMapper = spiToXs2aPaymentMapper;
        this.xs2aToSpiPaymentInfo = xs2aToSpiPaymentInfo;
        this.commonPaymentSpi = commonPaymentSpi;
    }

    @Override
    PaymentSpi<SpiPaymentInfo, SpiPaymentInitiationResponse> getSpiService() {
        return commonPaymentSpi;
    }

    @Override
    SpiPaymentInfo mapToSpiPayment(CommonPayment xs2aPayment, String paymentProduct) {
        return xs2aToSpiPaymentInfo.mapToSpiPaymentRequest(xs2aPayment, paymentProduct);
    }

    @Override
    CommonPaymentInitiationResponse mapToXs2aResponse(SpiPaymentInitiationResponse spiResponse, InitialSpiAspspConsentDataProvider provider, PaymentType paymentType) {
        return spiToXs2aPaymentMapper.mapToCommonPaymentInitiateResponse(spiResponse, paymentType, provider);
    }
}
