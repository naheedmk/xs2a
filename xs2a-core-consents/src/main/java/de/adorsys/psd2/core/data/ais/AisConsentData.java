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

package de.adorsys.psd2.core.data.ais;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@Value
public class AisConsentData {
    @NotNull
    private final AccountAccess tppAccountAccess;
    @NotNull
    private final AccountAccess aspspAccountAccess;
    private final boolean combinedServiceIndicator;

    @JsonIgnore
    public AisConsentRequestType getConsentRequestType() {
        AccountAccess usedAccess = getUsedAccess();
        return getRequestType(usedAccess.getAllPsd2(),
                              usedAccess.getAvailableAccounts(),
                              usedAccess.getAvailableAccountsWithBalance(),
                              !usedAccess.isNotEmpty());
    }

    @JsonIgnore
    public boolean isWithBalance() {
        return CollectionUtils.isNotEmpty(tppAccountAccess.getBalances());
    }

    @JsonIgnore
    public AccountAccess getUsedAccess() {
        if (tppAccountAccess.getAllPsd2() != null) {
            return tppAccountAccess;
        }

        if (aspspAccountAccess.isNotEmpty()) {
            return aspspAccountAccess;
        }

        return tppAccountAccess;
    }

    private AisConsentRequestType getRequestType(AccountAccessType allPsd2,
                                                 AccountAccessType availableAccounts,
                                                 AccountAccessType availableAccountsWithBalance,
                                                 boolean isAccessesEmpty) {

        List<AccountAccessType> allAccountsType = Arrays.asList(AccountAccessType.ALL_ACCOUNTS, AccountAccessType.ALL_ACCOUNTS_WITH_OWNER_NAME);

        if (allAccountsType.contains(allPsd2)) {
            return AisConsentRequestType.GLOBAL;
        } else if (allAccountsType.contains(availableAccounts)) {
            return AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS;
        } else if (allAccountsType.contains(availableAccountsWithBalance)) {
            return AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS;
        } else if (isAccessesEmpty) {
            return AisConsentRequestType.BANK_OFFERED;
        }
        return AisConsentRequestType.DEDICATED_ACCOUNTS;
    }
}
