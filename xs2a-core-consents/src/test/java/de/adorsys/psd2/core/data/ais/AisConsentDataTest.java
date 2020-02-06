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

import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AisConsentDataTest {
    private JsonReader jsonReader = new JsonReader();

    @Test
    void getConsentRequestType_bankOffered() {
        AisConsentData aisConsentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-bank-offered.json", AisConsentData.class);

        assertEquals(AisConsentRequestType.BANK_OFFERED, aisConsentData.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_bankOfferedEmptyArray() {
        AisConsentData aisConsentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-bank-offered-empty-array.json", AisConsentData.class);

        assertEquals(AisConsentRequestType.BANK_OFFERED, aisConsentData.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_tpp_allAvailableAccounts() {
        AisConsentData aisConsentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-tpp-all-available.json",
                                                                     AisConsentData.class);

        assertEquals(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS, aisConsentData.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_tpp_allAvailableAccountsWithOwnerName() {
        AisConsentData aisConsentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-tpp-all-available-owner-name.json",
                                                                     AisConsentData.class);

        assertEquals(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS, aisConsentData.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_tpp_allAvailableAccountsWithBalance() {
        AisConsentData aisConsentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-tpp-all-available-with-balance.json",
                                                                     AisConsentData.class);

        assertEquals(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS, aisConsentData.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_tpp_allAvailableAccountsWithBalanceAndOwnerName() {
        AisConsentData aisConsentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-tpp-all-available-with-balance-owner-name.json",
                                                                     AisConsentData.class);

        assertEquals(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS, aisConsentData.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_tpp_dedicated() {
        AisConsentData aisConsentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-tpp-dedicated.json",
                                                                     AisConsentData.class);

        assertEquals(AisConsentRequestType.DEDICATED_ACCOUNTS, aisConsentData.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_tpp_global() {
        AisConsentData aisConsentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-tpp-global.json",
                                                                     AisConsentData.class);

        assertEquals(AisConsentRequestType.GLOBAL, aisConsentData.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_tpp_globalWithOwnerName() {
        AisConsentData aisConsentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-tpp-global-owner-name.json",
                                                                     AisConsentData.class);

        assertEquals(AisConsentRequestType.GLOBAL, aisConsentData.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_aspsp_allAvailableAccounts() {
        AisConsentData aisConsentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-aspsp-all-available.json",
                                                                     AisConsentData.class);

        assertEquals(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS, aisConsentData.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_aspsp_allAvailableAccountsWithOwnerName() {
        AisConsentData aisConsentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-aspsp-all-available-owner-name.json",
                                                                     AisConsentData.class);

        assertEquals(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS, aisConsentData.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_aspsp_allAvailableAccountsWithBalance() {
        AisConsentData aisConsentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-aspsp-all-available-with-balance.json",
                                                                     AisConsentData.class);

        assertEquals(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS, aisConsentData.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_aspsp_allAvailableAccountsWithBalanceAndOwnerName() {
        AisConsentData aisConsentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-aspsp-all-available-with-balance-owner-name.json",
                                                                     AisConsentData.class);

        assertEquals(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS, aisConsentData.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_aspsp_dedicated() {
        AisConsentData aisConsentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-aspsp-dedicated.json",
                                                                     AisConsentData.class);

        assertEquals(AisConsentRequestType.DEDICATED_ACCOUNTS, aisConsentData.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_aspsp_global() {
        AisConsentData aisConsentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-aspsp-global.json",
                                                                     AisConsentData.class);

        assertEquals(AisConsentRequestType.GLOBAL, aisConsentData.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_aspsp_globalWithOwnerName() {
        AisConsentData aisConsentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-aspsp-global-owner-name.json",
                                                                     AisConsentData.class);

        assertEquals(AisConsentRequestType.GLOBAL, aisConsentData.getConsentRequestType());
    }

    @Test
    void getUsedAccess_emptyAccesses() {
        AisConsentData bankOfferedConsentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-bank-offered.json", AisConsentData.class);
        AccountAccess emptyAccess = new AccountAccess(null, null, null, null,
                                                      null, null, null);

        assertEquals(emptyAccess, bankOfferedConsentData.getUsedAccess());
    }

    @Test
    void getUsedAccess_tppAccess() {
        AisConsentData consentDataWithTppAccess = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-tpp-dedicated.json", AisConsentData.class);
        List<AccountReference> accountReferences = Collections.singletonList(new AccountReference(AccountReferenceType.IBAN, "DE98500105171757213183", null));
        AccountAccess dedicatedAccess = new AccountAccess(accountReferences, accountReferences, accountReferences, null,
                                                          null, null, null);

        assertEquals(dedicatedAccess, consentDataWithTppAccess.getUsedAccess());
    }

    @Test
    void getUsedAccess_aspspAccess() {
        AisConsentData consentDataWithAspspAccess = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-aspsp-dedicated.json", AisConsentData.class);
        List<AccountReference> accountReferences = Collections.singletonList(new AccountReference(AccountReferenceType.IBAN, "DE98500105171757213183", null));
        AccountAccess dedicatedAccess = new AccountAccess(accountReferences, accountReferences, accountReferences, null,
                                                          null, null, null);

        assertEquals(dedicatedAccess, consentDataWithAspspAccess.getUsedAccess());
    }

    @Test
    void getUsedAccess_globalConsentWithAspspReferences_shouldReturnTppAccess() {
        AisConsentData globalConsentDataWithAccountReferences = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data-global-aspsp-accounts.json", AisConsentData.class);
        AccountAccess globalAccess = new AccountAccess(null, null, null, null,
                                                       AccountAccessType.ALL_ACCOUNTS, null, null);

        assertEquals(globalAccess, globalConsentDataWithAccountReferences.getUsedAccess());
    }
}
