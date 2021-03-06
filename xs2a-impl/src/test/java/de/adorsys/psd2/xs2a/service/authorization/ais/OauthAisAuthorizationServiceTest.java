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

package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OauthAisAuthorizationServiceTest {
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("Test psuId", null, null, null, null);

    @InjectMocks
    private OauthAisAuthorizationService oauthAisAuthorizationService;

    @Test
    void createConsentAuthorization_success() {
        //When
        Optional<CreateConsentAuthorizationResponse> actualResponse = oauthAisAuthorizationService.createConsentAuthorization(PSU_ID_DATA, CONSENT_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getAccountConsentAuthorizationById_success() {
        //When
        Optional<Authorisation> actualResponse = oauthAisAuthorizationService.getAccountConsentAuthorizationById(AUTHORISATION_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getAuthorisationScaStatus_success() {
        //When
        Optional<ScaStatus> actualResponse = oauthAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getScaApproachServiceType_success() {
        //When
        ScaApproach actualResponse = oauthAisAuthorizationService.getScaApproachServiceType();

        //Then
        assertThat(actualResponse).isNotNull();
    }
}
