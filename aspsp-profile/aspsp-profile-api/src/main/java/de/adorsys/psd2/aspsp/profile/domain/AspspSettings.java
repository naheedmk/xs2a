/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.aspsp.profile.domain;

import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.profile.StartAuthorisationMode;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class AspspSettings {
    private int accountAccessFrequencyPerDay;
    private boolean aisPisSessionsSupported;
    private String aisRedirectUrlToAspsp;
    private long authorisationExpirationTimeMs;
    private boolean availableAccountsConsentSupported;
    private List<BookingStatus> availableBookingStatuses;
    private boolean bankOfferedConsentSupported;
    private boolean deltaListSupported;
    private boolean entryReferenceFromSupported;
    private boolean forceXs2aBaseLinksUrl;
    private boolean globalConsentSupported;
    private int maxConsentValidityDays;
    private int maxTransactionValidityDays;
    private MulticurrencyAccountLevel multicurrencyAccountLevelSupported;
    private long notConfirmedConsentExpirationTimeMs;
    private long notConfirmedPaymentExpirationTimeMs;
    private boolean paymentCancellationAuthorisationMandated;
    private long paymentCancellationRedirectUrlExpirationTimeMs;
    private boolean piisConsentSupported;
    private String pisPaymentCancellationRedirectUrlToAspsp;
    private String pisRedirectUrlToAspsp;
    private boolean psuInInitialRequestMandated;
    private long redirectUrlExpirationTimeMs;
    private boolean scaByOneTimeAvailableAccountsConsentRequired;
    private ScaRedirectFlow scaRedirectFlow;
    private boolean signingBasketSupported;
    private StartAuthorisationMode startAuthorisationMode;
    private List<SupportedAccountReferenceField> supportedAccountReferenceFields;
    private Map<PaymentType, Set<String>> supportedPaymentTypeAndProductMatrix;
    private String supportedTransactionApplicationTypes;
    private boolean tppSignatureRequired;
    private boolean transactionsWithoutBalancesSupported;
    private String xs2aBaseLinksUrl;
}
