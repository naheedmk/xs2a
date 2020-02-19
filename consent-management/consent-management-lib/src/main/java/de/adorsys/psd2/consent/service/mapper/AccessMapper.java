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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.AccountInfo;
import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.consent.api.ais.AccountAdditionalInformationAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.account.TppAccountAccess;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AccessMapper {

    public AccountAccess mapTppAccessesToAccountAccess(List<TppAccountAccess> tppAccountAccesses) {
        AccountAccess accountAccess = AccountAccess.EMPTY_ACCESS;
        tppAccountAccesses.forEach(a -> {
            AccountReference accountReference = new AccountReference(a.getAccountReferenceType(),
                                                                     a.getAccountIdentifier(),
                                                                     a.getCurrency());
            populateAccountAccesses(accountAccess, accountReference, a.getTypeAccess());
        });
        return accountAccess;
    }

    public AccountAccess mapAspspAccessesToAccountAccess(List<AspspAccountAccess> aspspAccountAccesses) {
        AccountAccess accountAccess = AccountAccess.EMPTY_ACCESS;
        aspspAccountAccesses.forEach(a -> {
            AccountReference accountReference = new AccountReference(a.getAccountReferenceType(),
                                                                     a.getAccountIdentifier(),
                                                                     a.getCurrency(),
                                                                     a.getResourceId(),
                                                                     a.getAspspAccountId());
            populateAccountAccesses(accountAccess, accountReference, a.getTypeAccess());
        });
        return accountAccess;
    }

    public List<TppAccountAccess> mapToTppAccountAccess(AccountAccess accountAccess) {
        List<TppAccountAccess> tppAccountAccesses = new ArrayList<>();
        tppAccountAccesses.addAll(accountAccess.getAccounts().stream().map(a -> new TppAccountAccess(a.getAccountReferenceType().getValue(),
                                                                                                     TypeAccess.ACCOUNT,
                                                                                                     a.getAccountReferenceType(),
                                                                                                     a.getCurrency())).collect(Collectors.toList()));
        tppAccountAccesses.addAll(accountAccess.getBalances().stream().map(a -> new TppAccountAccess(a.getAccountReferenceType().getValue(),
                                                                                                     TypeAccess.BALANCE,
                                                                                                     a.getAccountReferenceType(),
                                                                                                     a.getCurrency())).collect(Collectors.toList()));
        tppAccountAccesses.addAll(accountAccess.getTransactions().stream().map(a -> new TppAccountAccess(a.getAccountReferenceType().getValue(),
                                                                                                         TypeAccess.TRANSACTION,
                                                                                                         a.getAccountReferenceType(),
                                                                                                         a.getCurrency())).collect(Collectors.toList()));
        if (CollectionUtils.isNotEmpty(accountAccess.getAdditionalInformationAccess().getOwnerName())) {
            tppAccountAccesses.addAll(accountAccess.getAdditionalInformationAccess().getOwnerName().stream().map(a -> new TppAccountAccess(a.getAccountReferenceType().getValue(),
                                                                                                                                           TypeAccess.OWNER_NAME,
                                                                                                                                           a.getAccountReferenceType(),
                                                                                                                                           a.getCurrency())).collect(Collectors.toList()));
        }
        return tppAccountAccesses;
    }

    public List<AspspAccountAccess> mapToAspspAccountAccess(AccountAccess accountAccess) {
        List<AspspAccountAccess> aspspAccountAccesses = new ArrayList<>();
        aspspAccountAccesses.addAll(accountAccess.getAccounts().stream().map(a -> new AspspAccountAccess(a.getAccountReferenceType().getValue(),
                                                                                                         TypeAccess.ACCOUNT,
                                                                                                         a.getAccountReferenceType(),
                                                                                                         a.getCurrency(),
                                                                                                         a.getResourceId(),
                                                                                                         a.getAspspAccountId())).collect(Collectors.toList()));
        aspspAccountAccesses.addAll(accountAccess.getBalances().stream().map(a -> new AspspAccountAccess(a.getAccountReferenceType().getValue(),
                                                                                                         TypeAccess.BALANCE,
                                                                                                         a.getAccountReferenceType(),
                                                                                                         a.getCurrency(),
                                                                                                         a.getResourceId(),
                                                                                                         a.getAspspAccountId())).collect(Collectors.toList()));
        aspspAccountAccesses.addAll(accountAccess.getTransactions().stream().map(a -> new AspspAccountAccess(a.getAccountReferenceType().getValue(),
                                                                                                             TypeAccess.TRANSACTION,
                                                                                                             a.getAccountReferenceType(),
                                                                                                             a.getCurrency(),
                                                                                                             a.getResourceId(),
                                                                                                             a.getAspspAccountId())).collect(Collectors.toList()));
        if (CollectionUtils.isNotEmpty(accountAccess.getAdditionalInformationAccess().getOwnerName())) {
            aspspAccountAccesses.addAll(accountAccess.getAdditionalInformationAccess().getOwnerName().stream().map(a -> new AspspAccountAccess(a.getAccountReferenceType().getValue(),
                                                                                                                                               TypeAccess.OWNER_NAME,
                                                                                                                                               a.getAccountReferenceType(),
                                                                                                                                               a.getCurrency(),
                                                                                                                                               a.getResourceId(),
                                                                                                                                               a.getAspspAccountId())).collect(Collectors.toList()));
        }
        return aspspAccountAccesses;
    }

    public AccountAccess mapToAccountAccess(AisAccountAccessInfo aisAccountAccessInfo) {
        Set<AccountReference> accounts = mapToAccountReferences(aisAccountAccessInfo.getAccounts());
        Set<AccountReference> balances = mapToAccountReferences(aisAccountAccessInfo.getBalances());
        Set<AccountReference> transactions = mapToAccountReferences(aisAccountAccessInfo.getTransactions());

        Set<AccountReference> allAccounts = addReferencesToAccounts(accounts, balances, transactions);

        // ToDO fix enum values https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1170
        return new AccountAccess(new ArrayList<>(allAccounts),
                                 new ArrayList<>(balances),
                                 new ArrayList<>(transactions),
                                 mapToAdditionalInformationAccess(aisAccountAccessInfo.getAccountAdditionalInformationAccess()));
    }

    private AdditionalInformationAccess mapToAdditionalInformationAccess(AccountAdditionalInformationAccess accountAdditionalInformationAccess) {
        if (accountAdditionalInformationAccess == null) {
            return null;
        }

        List<AccountInfo> ownerNameAccountInfo = accountAdditionalInformationAccess.getOwnerName();
        if (ownerNameAccountInfo == null) {
            return new AdditionalInformationAccess(null);
        }

        List<AccountReference> ownerNameAccountReferences = ownerNameAccountInfo.stream().map(this::mapToAccountReference).collect(Collectors.toList());
        return new AdditionalInformationAccess(ownerNameAccountReferences);
    }

    private Set<AccountReference> mapToAccountReferences(@NotNull List<AccountInfo> accountInfoList) {
        return accountInfoList.stream()
                   .map(this::mapToAccountReference)
                   .collect(Collectors.toSet());
    }

    private AccountReference mapToAccountReference(AccountInfo accountInfo) {
        return new AccountReference(accountInfo.getAccountType(),
                                    accountInfo.getAccountIdentifier(),
                                    getCurrencyByString(accountInfo.getCurrency()),
                                    accountInfo.getResourceId(),
                                    accountInfo.getAspspAccountId());
    }

    private Currency getCurrencyByString(String currency) {
        return Optional.ofNullable(currency)
                   .map(Currency::getInstance)
                   .orElse(null);
    }

    private Set<AccountReference> addReferencesToAccounts(Set<AccountReference> accounts,
                                                          Set<AccountReference> balances,
                                                          Set<AccountReference> transactions) {
        return Stream.of(accounts, balances, transactions)
                   .flatMap(Collection::stream)
                   .collect(Collectors.toSet());
    }

    private void populateAccountAccesses(AccountAccess accountAccess, AccountReference accountReference, TypeAccess typeAccess) {
        if (TypeAccess.ACCOUNT == typeAccess) {
            accountAccess.getAccounts().add(accountReference);
        } else if (TypeAccess.BALANCE == typeAccess) {
            accountAccess.getBalances().add(accountReference);
        } else if (TypeAccess.TRANSACTION == typeAccess) {
            accountAccess.getTransactions().add(accountReference);
        } else if (TypeAccess.OWNER_NAME == typeAccess) {
            accountAccess.getAdditionalInformationAccess().getOwnerName().add(accountReference);
        }
    }
}
