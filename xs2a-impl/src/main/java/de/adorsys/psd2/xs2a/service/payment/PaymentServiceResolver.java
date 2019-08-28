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

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.config.factory.ReadPaymentFactory;
import de.adorsys.psd2.xs2a.config.factory.ReadPaymentStatusFactory;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.payment.create.*;
import de.adorsys.psd2.xs2a.service.payment.read.ReadCommonPaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadPaymentService;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Slf4j
@Component
@AllArgsConstructor
public class PaymentServiceResolver {
    private final StandardPaymentProductsResolver standardPaymentProductsResolver;

    private final CreateCommonPaymentService createCommonPaymentService;
    private final CreateSinglePaymentService createSinglePaymentService;
    private final CreatePeriodicPaymentService createPeriodicPaymentService;
    private final CreateBulkPaymentService createBulkPaymentService;

    private final ReadCommonPaymentService readCommonPaymentService;
    private final ReadPaymentFactory readPaymentFactory;

    private final ReadCommonPaymentStatusService readCommonPaymentStatusService;
    private final ReadPaymentStatusFactory readPaymentStatusFactory;

    private final ScaApproachResolver scaApproachResolver;

    public CreatePaymentService getCreatePaymentService(PaymentInitiationParameters paymentInitiationParameters) {
        if (isNotSupportedScaApproach(scaApproachResolver.resolveScaApproach())) {
            throw new UnsupportedOperationException("Unsupported operation");
        }

        if (standardPaymentProductsResolver.isRawPaymentProduct(paymentInitiationParameters.getPaymentProduct())) {
            return createCommonPaymentService;
        }

        if (PaymentType.SINGLE == paymentInitiationParameters.getPaymentType()) {
            return createSinglePaymentService;
        } else if (PaymentType.PERIODIC == paymentInitiationParameters.getPaymentType()) {
            return createPeriodicPaymentService;
        } else {
            return createBulkPaymentService;
        }
    }

    public ReadPaymentService getReadPaymentService(CommonPaymentData commonPaymentData) {
        if (commonPaymentData.getPaymentData() != null) {
            return readCommonPaymentService;
        }
        return readPaymentFactory.getService(commonPaymentData.getPaymentType().getValue());
    }

    public ReadPaymentStatusService getReadPaymentStatusService(PisCommonPaymentResponse pisCommonPaymentResponse) {
        if (pisCommonPaymentResponse.getPaymentData() != null) {
            return readCommonPaymentStatusService;
        }
        return readPaymentStatusFactory.getService(ReadPaymentStatusFactory.SERVICE_PREFIX + pisCommonPaymentResponse.getPaymentType().getValue());
    }

    private boolean isNotSupportedScaApproach(ScaApproach scaApproach) {
        return !EnumSet.of(ScaApproach.REDIRECT, ScaApproach.EMBEDDED, ScaApproach.DECOUPLED).contains(scaApproach);
    }
}
