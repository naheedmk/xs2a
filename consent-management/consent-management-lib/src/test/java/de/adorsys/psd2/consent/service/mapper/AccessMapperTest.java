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

import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.core.data.ais.AccountAccess;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccessMapperTest {

    @Test
    void mapToAccountAccess() {
        AccessMapper accessMapper = new AccessMapper();
        JsonReader jsonReader = new JsonReader();

        AisAccountAccessInfo accountAccessInfo = jsonReader.getObjectFromFile("json/service/mapper/ais-account-access-info.json", AisAccountAccessInfo.class);

        AccountAccess actual = accessMapper.mapToAccountAccess(accountAccessInfo);

        AccountAccess expected = jsonReader.getObjectFromFile("json/service/mapper/account-access.json", AccountAccess.class);

        actual.getAccounts().sort(Comparator.comparing(AccountReference::getResourceId));
        expected.getAccounts().sort(Comparator.comparing(AccountReference::getResourceId));

        assertEquals(expected, actual);
    }
}
