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

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.config.factory.ReadPaymentFactory;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.payment.create.*;
import de.adorsys.psd2.xs2a.service.payment.read.ReadCommonPaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadPaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadSinglePaymentService;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceResolverTest {

    @InjectMocks
    private PaymentServiceResolver paymentServiceResolver;

    @Mock
    private StandardPaymentProductsResolver standardPaymentProductsResolver;
    @Mock
    private CreateCommonPaymentService createCommonPaymentService;
    @Mock
    private CreateSinglePaymentService createSinglePaymentService;
    @Mock
    private CreatePeriodicPaymentService createPeriodicPaymentService;
    @Mock
    private CreateBulkPaymentService createBulkPaymentService;
    @Mock
    private ReadCommonPaymentService readCommonPaymentService;
    @Mock
    private ReadPaymentFactory readPaymentFactory;
    @Mock
    private ReadSinglePaymentService readSinglePaymentService;

    @Mock
    private ScaApproachResolver scaApproachResolver;

    private PaymentInitiationParameters paymentInitiationParameters;
    private PisCommonPaymentResponse commonPaymentData;

    @Before
    public void setUp() {
        paymentInitiationParameters = new PaymentInitiationParameters();
        commonPaymentData = new PisCommonPaymentResponse();
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
    }

    @Test
    public void getCreatePaymentService_commonPayment() {
        when(standardPaymentProductsResolver.isRawPaymentProduct(paymentInitiationParameters.getPaymentProduct())).thenReturn(true);

        CreatePaymentService createPaymentService = paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters);
        assertEquals(createCommonPaymentService, createPaymentService);
        assertTrue(createPaymentService instanceof CreateCommonPaymentService);
    }

    @Test
    public void getCreatePaymentService_singlePayment() {
        paymentInitiationParameters.setPaymentType(PaymentType.SINGLE);
        when(standardPaymentProductsResolver.isRawPaymentProduct(paymentInitiationParameters.getPaymentProduct())).thenReturn(false);

        CreatePaymentService createPaymentService = paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters);
        assertEquals(createSinglePaymentService, createPaymentService);
        assertTrue(createPaymentService instanceof CreateSinglePaymentService);
    }

    @Test
    public void getCreatePaymentService_periodicPayment() {
        paymentInitiationParameters.setPaymentType(PaymentType.PERIODIC);
        when(standardPaymentProductsResolver.isRawPaymentProduct(paymentInitiationParameters.getPaymentProduct())).thenReturn(false);

        CreatePaymentService createPaymentService = paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters);
        assertEquals(createPeriodicPaymentService, createPaymentService);
        assertTrue(createPaymentService instanceof CreatePeriodicPaymentService);
    }

    @Test
    public void getCreatePaymentService_bulkPayment() {
        paymentInitiationParameters.setPaymentType(PaymentType.BULK);
        when(standardPaymentProductsResolver.isRawPaymentProduct(paymentInitiationParameters.getPaymentProduct())).thenReturn(false);

        CreatePaymentService createPaymentService = paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters);
        assertEquals(createBulkPaymentService, createPaymentService);
        assertTrue(createPaymentService instanceof CreateBulkPaymentService);
    }

    @Test
    public void getCreatePaymentService_noPaymentType() {
        when(standardPaymentProductsResolver.isRawPaymentProduct(paymentInitiationParameters.getPaymentProduct())).thenReturn(false);

        CreatePaymentService createPaymentService = paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters);
        assertEquals(createBulkPaymentService, createPaymentService);
        assertTrue(createPaymentService instanceof CreateBulkPaymentService);
    }

    @Test
    public void getReadPaymentService_commonPayment() {
        commonPaymentData.setPaymentData("body".getBytes());
        ReadPaymentService readPaymentService = paymentServiceResolver.getReadPaymentService(commonPaymentData);
        assertTrue(readPaymentService instanceof ReadCommonPaymentService);
    }

    @Test
    public void getReadPaymentService_singlePayment() {
        commonPaymentData.setPaymentType(PaymentType.SINGLE);
        when(readPaymentFactory.getService(PaymentType.SINGLE.getValue())).thenReturn(readSinglePaymentService);

        ReadPaymentService readPaymentService = paymentServiceResolver.getReadPaymentService(commonPaymentData);
        assertTrue(readPaymentService instanceof ReadSinglePaymentService);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getCreatePaymentService_withOauthApproach() {
        // Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.OAUTH);

        // When
        paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getCreatePaymentService_withUndefinedApproach() {
        // Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.UNDEFINED);

        // When
        paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters);
    }
}
