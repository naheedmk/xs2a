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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentTransaction;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.repository.AisConsentTransactionRepository;
import de.adorsys.psd2.consent.repository.AisConsentUsageRepository;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OneOffConsentExpirationService {

    private final AisConsentUsageRepository aisConsentUsageRepository;
    private final AisConsentTransactionRepository aisConsentTransactionRepository;

    /**
     * Checks, should the one-off consent be expired after using its all GET endpoints (accounts, balances, transactions)
     * in all possible combinations.
     *
     * @param consent the {@link AisConsent} to check.
     * @return true if the consent should be expired, false otherwise.
     */
    public boolean isConsentExpired(AisConsent consent) {

        // We omit all bank offered consents until they are not populated with accounts.
        if (consent.getAisConsentRequestType() == AisConsentRequestType.BANK_OFFERED) {
            return false;
        }

        List<String> consentResourceIds = consent.getAspspAccountAccesses()
                                              .stream()
                                              .map(AspspAccountAccess::getResourceId)
                                              .distinct()
                                              .collect(Collectors.toList());

        boolean isExpired = false;
        for (String resourceId : consentResourceIds) {
            List<AisConsentTransaction> transactionList = aisConsentTransactionRepository.findByConsentIdAndResourceId(consent, resourceId);

            // If we do not have the account list yet - we can't read the transaction list for each resource ID. End of the loop
            // in this case.
            if (CollectionUtils.isEmpty(transactionList)) {
                break;
            }

            long transactions = transactionList.iterator().next().getNumberOfTransactions();

            long maximumNumberOfGetRequestsForConsent = getMaximumNumberOfGetRequestsForConsent(consent, consentResourceIds.size(), transactions);
            long numberOfUsedGetRequestsForConsent = aisConsentUsageRepository.countByConsentIdAndResourceId(consent.getId(), resourceId);

            // There are some available not used get requests.
            if (numberOfUsedGetRequestsForConsent < maximumNumberOfGetRequestsForConsent) {
                break;
            }

            isExpired = true;
        }

        return isExpired;
    }

    private long getMaximumNumberOfGetRequestsForConsent(AisConsent consent, long accountsNumber, long numberOfTransactions) {
        switch (consent.getAisConsentRequestType()) {
            case DEDICATED_ACCOUNTS:
            case GLOBAL:
                // Value 3 corresponds to the number of static get requests in scope of each account: readAccountDetails,
                // readBalances, readTransactionList.
                return accountsNumber * (3 + numberOfTransactions);
            case ALL_AVAILABLE_ACCOUNTS:
                return accountsNumber;
            case BANK_OFFERED:
                return 999999;
            default:
                return 0;
        }
    }


}
