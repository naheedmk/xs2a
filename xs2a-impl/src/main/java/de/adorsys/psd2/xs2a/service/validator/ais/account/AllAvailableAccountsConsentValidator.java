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
import org.springframework.stereotype.Service;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.CONSENT_INVALID;

@Service
public class AllAvailableAccountsConsentValidator {

    public ValidationResult validate(AisConsentRequestType aisConsentRequestType) {
        if (AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS == aisConsentRequestType) {
            return ValidationResult.invalid(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));
        }

        return ValidationResult.valid();
    }
}
