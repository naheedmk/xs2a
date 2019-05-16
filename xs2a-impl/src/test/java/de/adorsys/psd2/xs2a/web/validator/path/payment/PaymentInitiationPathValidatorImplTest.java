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

package de.adorsys.psd2.xs2a.web.validator.path.payment;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentInitiationPathValidatorImplTest {

    private static final String PAYMENT_SERVICE_PATH_VAR = "payment-service";
    private static final String PAYMENT_PRODUCT_PATH_VAR = "payment-product";

    private static final String CORRECT_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String WRONG_PAYMENT_PRODUCT = "sepa-credit-transfers111";

    private static final String PAYMENT_SERVICE = "payments";

    private PaymentInitiationPathValidatorImpl validator;
    private MessageError messageError;

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @Before
    public void setUp() {
        Map<PaymentType, Set<String>> supportedPaymentTypeAndProductMatrix = new HashMap<>();
        supportedPaymentTypeAndProductMatrix.put(PaymentType.SINGLE, Collections.singleton(CORRECT_PAYMENT_PRODUCT));
        when(aspspProfileServiceWrapper.getSupportedPaymentTypeAndProductMatrix()).thenReturn(supportedPaymentTypeAndProductMatrix);

        messageError = new MessageError();
        validator = new PaymentInitiationPathValidatorImpl(new ErrorBuildingServiceMock(ErrorType.PIS_400),
                                                           aspspProfileServiceWrapper);
    }

    @Test
    public void validatePaymentInitiation_shouldReturnSuccess() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(CORRECT_PAYMENT_PRODUCT);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validatePaymentInitiationWrongPaymentProduct_shouldReturnError() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(WRONG_PAYMENT_PRODUCT);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(1, messageError.getTppMessages().size());
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
    }

    private Map<String, String> buildTemplateVariables(String paymentProduct) {
        Map<String, String> templates = new HashMap<>();
        templates.put(PAYMENT_PRODUCT_PATH_VAR, paymentProduct);
        templates.put(PAYMENT_SERVICE_PATH_VAR, PAYMENT_SERVICE);
        return templates;
    }
}
