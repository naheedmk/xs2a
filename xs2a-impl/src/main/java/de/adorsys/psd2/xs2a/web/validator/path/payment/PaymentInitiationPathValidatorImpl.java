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
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.path.AbstractPathParameterValidatorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Component
public class PaymentInitiationPathValidatorImpl extends AbstractPathParameterValidatorImpl implements PaymentPathParameterValidator {

    private static final String PAYMENT_PRODUCT_PATH_VAR = "payment-product";
    private static final String PAYMENT_SERVICE_PATH_VAR = "payment-service";

    @Autowired
    public PaymentInitiationPathValidatorImpl(ErrorBuildingService errorBuildingService, AspspProfileServiceWrapper aspspProfileServiceWrapper) {
        super(errorBuildingService, aspspProfileServiceWrapper);
    }

    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {
        getRequestPathVariablesViolationMap(request).forEach((k, v) -> errorBuildingService.enrichMessageError(messageError, v));
    }

    private Map<String, String> getRequestPathVariablesViolationMap(HttpServletRequest request) {
        return new HashMap<>(getViolationMapForPaymentTypeAndPaymentProduct(request));
    }

    private Map<String, String> getViolationMapForPaymentTypeAndPaymentProduct(HttpServletRequest request) {
        Map<String, String> pathVariableMap = getPathVariableMap(request);
        Optional<PaymentType> paymentType = getPaymentTypeFromRequest(pathVariableMap);
        Optional<String> paymentProduct = getPaymentProductFromRequest(pathVariableMap);

        if (paymentType.isPresent() && paymentProduct.isPresent()) {
            return arePaymentTypeAndProductAvailable(paymentType.get(), paymentProduct.get());
        }

        return Collections.emptyMap();
    }

    private Optional<String> getPaymentProductFromRequest(Map<String, String> pathVariableMap) {
        return Optional.ofNullable(pathVariableMap)
                   .map(mp -> mp.get(PAYMENT_PRODUCT_PATH_VAR));
    }

    private Optional<PaymentType> getPaymentTypeFromRequest(Map<String, String> pathVariableMap) {
        return Optional.ofNullable(pathVariableMap)
                   .map(m -> m.get(PAYMENT_SERVICE_PATH_VAR))
                   .flatMap(PaymentType::getByValue);
    }

    private Map<String, String> arePaymentTypeAndProductAvailable(PaymentType paymentType, String paymentProduct) {
        Map<PaymentType, Set<String>> supportedPaymentTypeAndProductMatrix = aspspProfileServiceWrapper.getSupportedPaymentTypeAndProductMatrix();

        if (supportedPaymentTypeAndProductMatrix.containsKey(paymentType)) {
            if (supportedPaymentTypeAndProductMatrix.get(paymentType).contains(paymentProduct)) {
                return Collections.emptyMap();
            }
            return Collections.singletonMap(MessageErrorCode.PRODUCT_UNKNOWN.getName(), "Wrong payment product: " + paymentProduct);
        }
        return Collections.singletonMap(MessageErrorCode.PARAMETER_NOT_SUPPORTED.getName(), "Wrong payment type: " + paymentType.getValue());
    }
}
