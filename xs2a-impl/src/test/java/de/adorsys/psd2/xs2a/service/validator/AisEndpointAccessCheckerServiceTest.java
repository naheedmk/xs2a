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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AisEndpointAccessCheckerServiceTest {

    private static final String AUTHORISATION_ID = "11111111";
    private static final String CONSENT_ID = "22222222";
    private static final AccountConsentAuthorization CONSENT_AUTHORISATION_RECEIVED = buildAccountConsentAuthorization(ScaStatus.RECEIVED);
    private static final AccountConsentAuthorization CONSENT_AUTHORISATION_UNCONFIRMED = buildAccountConsentAuthorization(ScaStatus.UNCONFIRMED);

    @InjectMocks
    private AisEndpointAccessCheckerService aisEndpointAccessCheckerService;

    @Mock
    private Xs2aAisConsentService aisConsentService;

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    @Test
    public void isEndpointAccessible_Received_false() {

        when(aspspProfileService.isAuthorisationConfirmationRequestMandated())
            .thenReturn(true);

        when(aisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(Optional.of(CONSENT_AUTHORISATION_RECEIVED));

        boolean actual = aisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID, true);

        assertFalse(actual);
    }

    @Test
    public void isEndpointAccessible_Unconfirmed_true() {

        when(aspspProfileService.isAuthorisationConfirmationRequestMandated())
            .thenReturn(true);

        when(aisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(Optional.of(CONSENT_AUTHORISATION_UNCONFIRMED));

        boolean actual = aisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID, true);

        assertTrue(actual);
    }

    private static AccountConsentAuthorization buildAccountConsentAuthorization(ScaStatus scaStatus) {
        AccountConsentAuthorization accountConsentAuthorization = new AccountConsentAuthorization();
        accountConsentAuthorization.setChosenScaApproach(ScaApproach.REDIRECT);
        accountConsentAuthorization.setScaStatus(scaStatus);
        return accountConsentAuthorization;
    }

}
