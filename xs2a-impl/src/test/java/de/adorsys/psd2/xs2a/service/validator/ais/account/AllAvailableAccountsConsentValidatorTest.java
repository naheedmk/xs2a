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

package de.adorsys.psd2.xs2a.service.validator.ais.account;

import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.CONSENT_INVALID;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class AllAvailableAccountsConsentValidatorTest {
    @InjectMocks
    private AllAvailableAccountsConsentValidator allAvailableAccountsConsentValidator;

    @Test
    public void validate_AllAvailableAccountsType() {
        //Given
        AisConsentRequestType aisConsentRequestType = AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS;
        //When
        ValidationResult validationResult = allAvailableAccountsConsentValidator.validate(aisConsentRequestType);
        //Then
        assertEquals(ValidationResult.invalid(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID)), validationResult);
    }

    @Test
    public void validate_OtherAisConsentRequestType() {
        //Given
        //When
        Set<ValidationResult> validationResults = Arrays.stream(AisConsentRequestType.values())
                                                      .filter(type -> type != AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS)
                                                      .map(allAvailableAccountsConsentValidator::validate)
                                                      .collect(Collectors.toSet());
        //Then
        assertEquals(1, validationResults.size());
        assertEquals(ValidationResult.valid(), validationResults.iterator().next());
    }
}
