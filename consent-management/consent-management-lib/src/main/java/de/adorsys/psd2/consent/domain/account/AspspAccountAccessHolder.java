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
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Currency;
import java.util.EnumSet;
import java.util.List;

import static de.adorsys.psd2.consent.api.TypeAccess.*;

@Value
public class AspspAccountAccessHolder extends AccountAccessHolder<AspspAccountAccess> {
    public AspspAccountAccessHolder(AccountAccess accountAccess) {
        super(accountAccess);
    }

    @Override
    public void doFillAccess(List<AccountReference> accountReferences, TypeAccess typeAccess) {
        if (CollectionUtils.isNotEmpty(accountReferences)) {
            accountReferences.forEach(a -> addAccountAccess(a.getAspspAccountId(),
                                                            a.getUsedAccountReferenceSelector().getAccountValue(),
                                                            a.getResourceId(),
                                                            a.getCurrency(),
                                                            a.getAccountReferenceType(),
                                                            typeAccess));
        }
    }

    /**
     * According to specification if user gives access to TRANSACTION or BALANCE the access to accounts is granted automatically
     *
     * @param aspspAccountId       Bank specific account ID
     * @param accountIdentifier    Account-Identifier
     * @param resourceId           This identification is denoting the addressed account
     * @param currency             ISO 4217 currency code
     * @param accountReferenceType Type of the account reference
     * @param typeAccess           Type access
     */
    private void addAccountAccess(String aspspAccountId, String accountIdentifier, String resourceId, Currency currency, AccountReferenceType accountReferenceType, TypeAccess typeAccess) {
        accountAccesses.add(new AspspAccountAccess(accountIdentifier, typeAccess, accountReferenceType, currency, resourceId, aspspAccountId));
        if (EnumSet.of(BALANCE, TRANSACTION).contains(typeAccess)) {
            accountAccesses.add(new AspspAccountAccess(accountIdentifier, ACCOUNT, accountReferenceType, currency, resourceId, aspspAccountId));
        }
    }
}
