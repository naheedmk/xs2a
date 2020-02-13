/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.payment.common;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiConfirmationCode;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PisCheckAuthorisationConfirmationServiceCommonImplTest {
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";

    @Mock
    private CommonPaymentSpi commonPaymentSpi;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;

    @InjectMocks
    private PisCheckAuthorisationConfirmationServiceCommonImpl pisCheckAuthorisationConfirmationServiceCommon;

    @Test
    void checkConfirmationCode() {
        // Given
        SpiContextData spiContextData = new SpiContextData(null, null, null, null, null);
        SpiConfirmationCode spiConfirmationCode = new SpiConfirmationCode("some code");
        SpiPaymentInfo payment = new SpiPaymentInfo(PAYMENT_PRODUCT);

        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> commonServiceResponse = SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder()
                                                                                              .payload(new SpiPaymentConfirmationCodeValidationResponse(ScaStatus.FINALISED, TransactionStatus.ACSP))
                                                                                              .build();
        when(commonPaymentSpi.checkConfirmationCode(spiContextData, spiConfirmationCode, AUTHORISATION_ID, spiAspspConsentDataProvider))
            .thenReturn(commonServiceResponse);

        // When
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> actualResponse =
            pisCheckAuthorisationConfirmationServiceCommon.checkConfirmationCode(spiContextData, spiConfirmationCode, payment, AUTHORISATION_ID, spiAspspConsentDataProvider);

        // Then
        assertEquals(commonServiceResponse, actualResponse);
    }
}
