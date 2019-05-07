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

package de.adorsys.psd2.xs2a.web.header;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentHeadersBuilder {
    private final ScaApproachResolver scaApproachResolver;

    public ResponseHeaders buildInitiatePaymentHeaders(@Nullable String authorisationId, @NotNull String selfLink) {
        ScaApproach scaApproach = authorisationId == null
                                      ? scaApproachResolver.resolveScaApproach()
                                      : scaApproachResolver.getInitiationScaApproach(authorisationId);

        return ResponseHeaders.builder()
                   .aspspScaApproach(scaApproach)
                   .location(selfLink)
                   .build();
    }

    public ResponseHeaders buildStartPaymentAuthorisationHeaders(@NotNull String authorisationId) {
        return buildAuthorisationHeaders(authorisationId);
    }

    public ResponseHeaders buildUpdatePaymentInitiationPsuDataHeaders(@NotNull String authorisationId) {
        return buildAuthorisationHeaders(authorisationId);
    }


    public ResponseHeaders buildStartPaymentCancellationAuthorisationHeaders(@NotNull String authorisationId) {
        return buildCancellationAuthorisationHeaders(authorisationId);
    }

    public ResponseHeaders buildUpdatePaymentCancellationPsuDataHeaders(@NotNull String authorisationId) {
        return buildCancellationAuthorisationHeaders(authorisationId);
    }

    private ResponseHeaders buildAuthorisationHeaders(String authorisationId) {
        ScaApproach scaApproach = scaApproachResolver.getInitiationScaApproach(authorisationId);
        return ResponseHeaders.builder()
                   .aspspScaApproach(scaApproach)
                   .build();
    }

    private ResponseHeaders buildCancellationAuthorisationHeaders(String authorisationId) {
        ScaApproach scaApproach = scaApproachResolver.getCancellationScaApproach(authorisationId);
        return ResponseHeaders.builder()
                   .aspspScaApproach(scaApproach)
                   .build();
    }
}
