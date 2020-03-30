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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrustedBeneficiariesInformationServiceTest {
    @InjectMocks
    private TrustedBeneficiariesInformationService trustedBeneficiariesInformationService;

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void checkSupportedAccountTrustedBeneficiariesInformation_supported() {
        // Given
        CreateConsentReq input = jsonReader.getObjectFromFile("json/service/create-consent-req-with-beneficiaries.json", CreateConsentReq.class);
        when(aspspProfileService.isTrustedBeneficiariesSupported()).thenReturn(true);

        // When
        CreateConsentReq actual = trustedBeneficiariesInformationService.checkIfAdditionalInformationSupported(input);

        // Then
        assertThat(actual).isEqualTo(input);
    }

    @Test
    void checkSupportedAccountTrustedBeneficiariesInformation_notSupportedAndPresent() {
        // Given
        CreateConsentReq input = jsonReader.getObjectFromFile("json/service/create-consent-req-with-beneficiaries.json", CreateConsentReq.class);
        CreateConsentReq expected = jsonReader.getObjectFromFile("json/service/create-consent-req.json", CreateConsentReq.class);
        when(aspspProfileService.isTrustedBeneficiariesSupported()).thenReturn(false);

        // When
        CreateConsentReq actual = trustedBeneficiariesInformationService.checkIfAdditionalInformationSupported(input);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void checkSupportedAccountTrustedBeneficiariesInformation_notSupportedAndNotPresent() {
        // Given
        CreateConsentReq input = jsonReader.getObjectFromFile("json/service/create-consent-req.json", CreateConsentReq.class);
        when(aspspProfileService.isTrustedBeneficiariesSupported()).thenReturn(false);

        // When
        CreateConsentReq actual = trustedBeneficiariesInformationService.checkIfAdditionalInformationSupported(input);

        // Then
        assertThat(actual).isEqualTo(input);
    }
}
