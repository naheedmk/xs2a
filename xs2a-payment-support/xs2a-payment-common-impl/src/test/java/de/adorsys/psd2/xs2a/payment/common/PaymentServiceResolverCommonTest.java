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

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.payment.cancel.CancelPaymentService;
import de.adorsys.psd2.xs2a.service.payment.create.CreateCommonPaymentService;
import de.adorsys.psd2.xs2a.service.payment.create.CreatePaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadCommonPaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadPaymentService;
import de.adorsys.psd2.xs2a.service.payment.status.ReadCommonPaymentStatusService;
import de.adorsys.psd2.xs2a.service.payment.status.ReadPaymentStatusService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceResolverCommonTest {
    @Mock
    private CreateCommonPaymentService createCommonPaymentService;
    @Mock
    private ReadCommonPaymentService readCommonPaymentService;
    @Mock
    private ReadCommonPaymentStatusService readCommonPaymentStatusService;
    @Mock
    private CancelPaymentService cancelCommonPaymentService;

    @InjectMocks
    private PaymentServiceResolverCommon paymentServiceResolverCommon;

    @Test
    public void getCreatePaymentService() {
        // When
        CreatePaymentService actualService =
            paymentServiceResolverCommon.getCreatePaymentService(new PaymentInitiationParameters());

        // Then
        assertEquals(createCommonPaymentService, actualService);
    }

    @Test
    public void getReadPaymentService() {
        // When
        ReadPaymentService actualService =
            paymentServiceResolverCommon.getReadPaymentService(new PisCommonPaymentResponse());

        // Then
        assertEquals(readCommonPaymentService, actualService);
    }

    @Test
    public void getReadPaymentStatusService() {
        // When
        ReadPaymentStatusService actualService =
            paymentServiceResolverCommon.getReadPaymentStatusService(new PisCommonPaymentResponse());

        // Then
        assertEquals(readCommonPaymentStatusService, actualService);
    }

    @Test
    public void getCancelPaymentService() {
        // Given
        PisPaymentCancellationRequest paymentCancellationRequest =
            new PisPaymentCancellationRequest(null, null, null, null, null);

        // When
        CancelPaymentService actualService =
            paymentServiceResolverCommon.getCancelPaymentService(paymentCancellationRequest);

        // Then
        assertEquals(cancelCommonPaymentService, actualService);
    }
}
