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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentTransaction;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AisConsentTransactionRepository;
import de.adorsys.psd2.consent.repository.AisConsentUsageRepository;
import de.adorsys.psd2.core.data.ais.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OneOffConsentExpirationService {

    private final AisConsentUsageRepository aisConsentUsageRepository;
    private final AisConsentTransactionRepository aisConsentTransactionRepository;
    private final ConsentDataMapper consentDataMapper;

    /**
     * Checks, should the one-off consent be expired after using its all GET endpoints (accounts, balances, transactions)
     * in all possible combinations depending on the consent type.
     *
     * @param consent the {@link ConsentEntity} to check.
     * @param cmsConsent the {@link CmsConsent} to check.
     * @return true if the consent should be expired, false otherwise.
     */

    // TODO need to refactor this method in order to use one parameter instead of two  TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1202
    public boolean isConsentExpired(ConsentEntity consent, CmsConsent cmsConsent) {
        byte[] consentData = cmsConsent.getConsentData();
        AisConsentData aisConsentData = consentDataMapper.mapToAisConsentData(consentData);
        AisConsentRequestType consentRequestType = aisConsentData.getConsentRequestType();

        // We omit all bank offered consents until they are not populated with accounts.
        if (consentRequestType == AisConsentRequestType.BANK_OFFERED) {
            return false;
        }

        // All available account consent support only one call - readAccountList.
        if (consentRequestType == AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS) {
            return true;
        }

        AccountAccess aspspAccess = aisConsentData.getAspspAccountAccess();
        List<AccountReference> references = Stream.of(aspspAccess.getAccounts(), aspspAccess.getBalances(), aspspAccess.getTransactions())
                                                .flatMap(Collection::stream).collect(Collectors.toList());

        List<String> consentResourceIds = references.stream()
                                              .map(AccountReference::getResourceId)
                                              .distinct()
                                              .collect(Collectors.toList());

        boolean isExpired = true;
        for (String resourceId : consentResourceIds) {
            Optional<AisConsentTransaction> transactionOptional = aisConsentTransactionRepository.findByConsentIdAndResourceId(consent, resourceId);

            int transactions = transactionOptional
                                   .map(AisConsentTransaction::getNumberOfTransactions)
                                   .orElse(0);

            int maximumNumberOfGetRequestsForConsent = getMaximumNumberOfGetRequestsForConsentsAccount(aspspAccess, resourceId, transactions);
            int numberOfUsedGetRequestsForConsent = aisConsentUsageRepository.countByConsentIdAndResourceId(consent.getId(), resourceId);

            // There are some available not used get requests - omit all other iterations.
            if (numberOfUsedGetRequestsForConsent < maximumNumberOfGetRequestsForConsent) {
                isExpired = false;
                break;
            }
        }

        return isExpired;
    }

    /**
     * This method returns maximum number of possible get requests for the definite consent for ONE account
     * except the main get call - readAccountList.
     */
    private int getMaximumNumberOfGetRequestsForConsentsAccount(AccountAccess aspspAccountAccesses, String resourceId, int numberOfTransactions) {

        boolean accessesForAccountsEmpty = aspspAccountAccesses.getAccounts().stream()
                                               .filter(access -> access.getResourceId().equals(resourceId))
                                               .collect(Collectors.toList()).isEmpty();

        boolean accessesForBalanceEmpty = aspspAccountAccesses.getBalances().stream()
                                              .filter(access -> access.getResourceId().equals(resourceId))
                                              .collect(Collectors.toList()).isEmpty();

        boolean accessesForTransactionsEmpty = aspspAccountAccesses.getTransactions().stream()
                                                   .filter(access -> access.getResourceId().equals(resourceId))
                                                   .collect(Collectors.toList()).isEmpty();

        // Consent was given only for accounts: readAccountDetails for each account.
        if (!accessesForAccountsEmpty
                && accessesForBalanceEmpty
                && accessesForTransactionsEmpty) {
            return 1;
        }

        // Consent was given for accounts and balances.
        if (accessesForTransactionsEmpty) {

            // Value 2 corresponds to the readAccountDetails and readBalances.
            return 2;
        }

        // Consent was given for accounts and transactions.
        if (accessesForBalanceEmpty) {

            // Value 2 corresponds to the readAccountDetails and readTransactions. Plus each account's transactions.
            return 2 + numberOfTransactions;
        }

        // Consent was given for accounts, balances and transactions.
        return 3 + numberOfTransactions;
    }
}
