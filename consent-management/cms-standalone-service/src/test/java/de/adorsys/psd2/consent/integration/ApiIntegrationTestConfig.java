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

package de.adorsys.psd2.consent.integration;

import de.adorsys.psd2.consent.config.CryptoConfig;
import de.adorsys.psd2.consent.repository.CryptoAlgorithmRepository;
import de.adorsys.psd2.consent.service.security.provider.CryptoProviderHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class ApiIntegrationTestConfig {

    @Bean
    public CryptoConfig cryptoConfig() {
        return new CryptoConfig() {
            @Override
            public CryptoProviderHolder initCryptoProviders(CryptoAlgorithmRepository cryptoAlgorithmRepository) {
                return new CryptoProviderHolder(new HashMap<>(), "", "");
            }
        };
    }
}
