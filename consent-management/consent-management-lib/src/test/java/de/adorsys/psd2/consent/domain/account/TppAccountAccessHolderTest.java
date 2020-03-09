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

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TppAccountAccessHolderTest {
    private static final String IBAN = "DE85500105178874624792";

    @Test
    void tppAccountAccessHolder_noAdditionalInformation() {
        //Given
        AccountAccess accountAccess = buildAccountAccessWithAdditionalInformation(buildEmptyAdditionalInformationAccess());
        //When
        TppAccountAccessHolder tppAccountAccessHolder = new TppAccountAccessHolder(accountAccess);
        //Then
        Set<TppAccountAccess> accountAccesses = tppAccountAccessHolder.getAccountAccesses();
        assertEquals(0, accountAccesses.size());
    }

    @Test
    void tppAccountAccessHolder_ownerName() {
        //Given
        AccountReference accountInfo = new AccountReference();
        accountInfo.setIban(IBAN);
        
        List<AccountReference> ownerName = Collections.singletonList(accountInfo);
        AccountAccess accountAccess = buildAccountAccessWithAdditionalInformation(buildAdditionalInformationAccess(ownerName));
        //When
        TppAccountAccessHolder tppAccountAccessHolder = new TppAccountAccessHolder(accountAccess);
        //Then
        Set<TppAccountAccess> accountAccesses = tppAccountAccessHolder.getAccountAccesses();
        assertEquals(1, accountAccesses.size());
        TppAccountAccess tppAccountAccess = accountAccesses.iterator().next();
        assertEquals(IBAN, tppAccountAccess.getAccountIdentifier());
        assertEquals(AccountReferenceType.IBAN, tppAccountAccess.getAccountReferenceType());
    }

    private AccountAccess buildAccountAccessWithAdditionalInformation(AdditionalInformationAccess additionalInformationAccess) {
        return new AccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), additionalInformationAccess);
    }

    private AdditionalInformationAccess buildEmptyAdditionalInformationAccess() {
        return new AdditionalInformationAccess(Collections.emptyList());
    }

    private AdditionalInformationAccess buildAdditionalInformationAccess(List<AccountReference> ownerNames) {
        return new AdditionalInformationAccess(ownerNames);
    }
}
