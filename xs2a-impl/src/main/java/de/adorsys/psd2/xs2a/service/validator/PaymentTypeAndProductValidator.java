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
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.PARAMETER_NOT_SUPPORTED;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.PRODUCT_UNKNOWN;

@Component
@RequiredArgsConstructor
public class PaymentTypeAndProductValidator implements BusinessValidator<PaymentInitiationParameters> {

    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ServiceTypeToErrorTypeMapper errorTypeMapper;
    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @Override
    public @NotNull ValidationResult validate(@NotNull PaymentInitiationParameters parameters) {
        return arePaymentTypeAndProductCorrect(parameters.getPaymentType(), parameters.getPaymentProduct());
    }

    private ValidationResult arePaymentTypeAndProductCorrect(PaymentType paymentType, String paymentProduct) {
        Map<PaymentType, Set<String>> supportedPaymentTypeAndProductMatrix = aspspProfileServiceWrapper.getSupportedPaymentTypeAndProductMatrix();

        if (supportedPaymentTypeAndProductMatrix.containsKey(paymentType)) {
            if (supportedPaymentTypeAndProductMatrix.get(paymentType).contains(paymentProduct)) {
                return ValidationResult.valid();
            }
            // Case when URL contains something like "/sepa-credit-transfers111/". Bad product.
            ErrorType errorType = errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(), PRODUCT_UNKNOWN.getCode());
            return ValidationResult.invalid(errorType, TppMessageInformation.of(PRODUCT_UNKNOWN, "Wrong payment product: " + paymentProduct));
        }
        // Case when URL contains correct type "/v1/payments/", but it is not supported by ASPSP. Bad type.
        ErrorType errorType = errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(), PARAMETER_NOT_SUPPORTED.getCode());
        return ValidationResult.invalid(errorType, TppMessageInformation.of(PARAMETER_NOT_SUPPORTED, "Wrong payment type: " + paymentType));
    }

}
