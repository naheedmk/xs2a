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

package de.adorsys.psd2.consent.domain.account;

import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.adorsys.psd2.consent.api.TypeAccess.*;

@Getter
public abstract class AccountAccessHolder<T extends AccountAccess> {
    protected Set<T> accountAccesses = new HashSet<>();
    private de.adorsys.psd2.core.data.AccountAccess accountAccessInfo;

    public AccountAccessHolder(de.adorsys.psd2.core.data.AccountAccess accountAccessInfo) {
        this.accountAccessInfo = accountAccessInfo;
        fillAccess(this.accountAccessInfo);
    }

    private void fillAccess(de.adorsys.psd2.core.data.AccountAccess accountAccess) {
        doFillAccess(accountAccess.getAccounts(), ACCOUNT);
        doFillAccess(accountAccess.getBalances(), BALANCE);
        doFillAccess(accountAccess.getTransactions(), TRANSACTION);
        AdditionalInformationAccess accountAdditionalInformationAccess = accountAccess.getAdditionalInformationAccess();
        if (accountAdditionalInformationAccess != null) {
            doFillAccess(accountAdditionalInformationAccess.getOwnerName(), OWNER_NAME);
        }
    }

    protected abstract void doFillAccess(List<AccountReference> info, TypeAccess typeAccess);
}
