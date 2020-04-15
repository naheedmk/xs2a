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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.model.Address;
import de.adorsys.psd2.model.TrustedBeneficiaries;
import de.adorsys.psd2.model.TrustedBeneficiariesList;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTrustedBeneficiaries;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTrustedBeneficiariesList;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aAddressMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TrustedBeneficiariesModelMapperImpl.class, TrustedBeneficiariesModelMapperTest.TestConfiguration.class})
class TrustedBeneficiariesModelMapperTest {

    @Autowired
    private TrustedBeneficiariesModelMapper trustedBeneficiariesModelMapper;
    @Autowired
    private Xs2aAddressMapper xs2aAddressMapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToTrustedBeneficiaries() {
        // Given
        TrustedBeneficiaries expected = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/trusted-beneficiaries.json", TrustedBeneficiaries.class);
        Xs2aTrustedBeneficiaries input = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/xs2a-trusted-beneficiaries.json", Xs2aTrustedBeneficiaries.class);
        Xs2aAddress xs2aAddress = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/xs2a-address.json", Xs2aAddress.class);
        Address address = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/address.json", Address.class);
        when(xs2aAddressMapper.mapToAddress(xs2aAddress)).thenReturn(address);

        // When
        TrustedBeneficiaries actual = trustedBeneficiariesModelMapper.mapToTrustedBeneficiaries(input);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToTrustedBeneficiariesList() {
        // Given
        TrustedBeneficiariesList expected = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/trusted-beneficiaries-list.json", TrustedBeneficiariesList.class);

        Xs2aTrustedBeneficiariesList input = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/xs2a-trusted-beneficiaries-list.json", Xs2aTrustedBeneficiariesList.class);

        Xs2aAddress xs2aAddress = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/xs2a-address.json", Xs2aAddress.class);
        Address address = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/address.json", Address.class);
        when(xs2aAddressMapper.mapToAddress(xs2aAddress)).thenReturn(address);

        // When
        TrustedBeneficiariesList actual = trustedBeneficiariesModelMapper.mapToTrustedBeneficiariesList(input);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Configuration
    static class TestConfiguration {
        @Bean
        public Xs2aAddressMapper mockXs2aAddressMapper() {
            return mock(Xs2aAddressMapper.class);
        }
    }
}
