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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.PARAMETER_NOT_SUPPORTED;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.PRODUCT_UNKNOWN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentProductAndTypeValidatorTest {

    private static final ServiceType SERVICE_TYPE = ServiceType.PIS;
    private static final String CORRECT_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String WRONG_PAYMENT_PRODUCT = "sepa-credit-transfers111";
    private static final PaymentType UNSUPPORTED_PAYMENT_TYPE = PaymentType.PERIODIC;

    @InjectMocks
    private PaymentTypeAndProductValidator paymentProductAndTypeValidator;

    @Mock
    private ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    @Mock
    private ServiceTypeToErrorTypeMapper errorTypeMapper;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    @Before
    public void setUp() {
        Map<PaymentType, Set<String>> matrix = getSupportedPaymentTypeAndProductMatrix();
        when(aspspProfileService.getSupportedPaymentTypeAndProductMatrix()).thenReturn(matrix);
        when(serviceTypeDiscoveryService.getServiceType()).thenReturn(SERVICE_TYPE);
    }

    @Test
    public void validatePaymentInitiationParams_correct() {
        //Given:
        when(errorTypeMapper.mapToErrorType(SERVICE_TYPE, PRODUCT_UNKNOWN.getCode())).thenReturn(ErrorType.PIS_404);

        PaymentInitiationParameters parameters = new PaymentInitiationParameters();
        parameters.setPaymentType(PaymentType.SINGLE);
        parameters.setPaymentProduct(CORRECT_PAYMENT_PRODUCT);

        //When:
        ValidationResult actual = paymentProductAndTypeValidator.validate(parameters);

        //Then:
        assertTrue(actual.isValid());
    }

    @Test
    public void validatePaymentInitiationParams_wrongProduct() {
        //Given:
        when(errorTypeMapper.mapToErrorType(SERVICE_TYPE, PRODUCT_UNKNOWN.getCode())).thenReturn(ErrorType.PIS_404);

        PaymentInitiationParameters parameters = new PaymentInitiationParameters();
        parameters.setPaymentType(PaymentType.SINGLE);
        parameters.setPaymentProduct(WRONG_PAYMENT_PRODUCT);

        //When:
        ValidationResult actual = paymentProductAndTypeValidator.validate(parameters);

        //Then:
        assertTrue(actual.isNotValid());
        assertEquals(1, actual.getMessageError().getTppMessages().size());
        assertEquals(MessageErrorCode.PRODUCT_UNKNOWN, actual.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void validatePaymentInitiationParams_unsupportedType() {
        //Given:
        when(errorTypeMapper.mapToErrorType(SERVICE_TYPE, PARAMETER_NOT_SUPPORTED.getCode())).thenReturn(ErrorType.PIS_400);

        PaymentInitiationParameters parameters = new PaymentInitiationParameters();
        parameters.setPaymentType(UNSUPPORTED_PAYMENT_TYPE);
        parameters.setPaymentProduct(CORRECT_PAYMENT_PRODUCT);

        //When:
        ValidationResult actual = paymentProductAndTypeValidator.validate(parameters);

        //Then:
        assertTrue(actual.isNotValid());
        assertEquals(1, actual.getMessageError().getTppMessages().size());
        assertEquals(PARAMETER_NOT_SUPPORTED, actual.getMessageError().getTppMessage().getMessageErrorCode());
    }

    private Map<PaymentType, Set<String>> getSupportedPaymentTypeAndProductMatrix() {
        Map<PaymentType, Set<String>> matrix = new HashMap<>();
        Set<String> availablePaymentProducts = new HashSet<>(Collections.singletonList(CORRECT_PAYMENT_PRODUCT));
        matrix.put(PaymentType.SINGLE, availablePaymentProducts);
        matrix.put(PaymentType.BULK, availablePaymentProducts);
        return matrix;
    }
}
