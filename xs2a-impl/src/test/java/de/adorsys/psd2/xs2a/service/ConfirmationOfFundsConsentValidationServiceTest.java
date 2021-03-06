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

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.piis.CommonConfirmationOfFundsConsentObject;
import de.adorsys.psd2.xs2a.service.validator.piis.DeleteConfirmationOfFundsConsentByIdValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmationOfFundsConsentValidationServiceTest {
    @InjectMocks
    private ConfirmationOfFundsConsentValidationService service;
    @Mock
    private DeleteConfirmationOfFundsConsentByIdValidator deleteConfirmationOfFundsConsentByIdValidator;
    @Mock
    private PiisConsent piisConsent;

    @Test
    void validateConsentOnDelete() {
        //Given
        ArgumentCaptor<CommonConfirmationOfFundsConsentObject> argumentCaptor = ArgumentCaptor.forClass(CommonConfirmationOfFundsConsentObject.class);
        when(deleteConfirmationOfFundsConsentByIdValidator.validate(argumentCaptor.capture())).thenReturn(ValidationResult.valid());
        //When
        service.validateConsentOnDelete(piisConsent);
        //Then
        verify(deleteConfirmationOfFundsConsentByIdValidator).validate(any(CommonConfirmationOfFundsConsentObject.class));
        assertEquals(piisConsent, argumentCaptor.getValue().getPiisConsent());
    }


}
