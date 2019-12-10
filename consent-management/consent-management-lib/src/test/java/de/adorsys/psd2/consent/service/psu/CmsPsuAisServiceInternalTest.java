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

package de.adorsys.psd2.consent.service.psu;

import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.repository.AisConsentAuthorisationRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentAuthorizationSpecification;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CmsPsuAisServiceInternalTest {
    private JsonReader jsonReader = new JsonReader();
    private static final String AUTHORISATION_ID = "a06248fe-9e79-48cb-93c4-4807e56207fe";
    private static final AuthenticationDataHolder AUTHENTICATION_DATA_HOLDER = new AuthenticationDataHolder("method id ", "123456");
    private static final String INSTANCE_ID = "UNDEFINED";

    @InjectMocks
    private CmsPsuAisServiceInternal cmsPsuAisServiceInternal;

    @Mock
    private AisConsentAuthorisationRepository aisConsentAuthorisationRepository;

    @Mock
    private AisConsentAuthorizationSpecification aisConsentAuthorizationSpecification;

    @Test
    public void setScaAuthenticationData_success() throws AuthorisationIsExpiredException {
        // given
        when(aisConsentAuthorisationRepository.findOne(any())).thenReturn(Optional.of(buildAisConsentAuthorisation()));
        AisConsentAuthorization expectedAuthorization = buildAisConsentAuthorisation();
        expectedAuthorization.setScaAuthenticationData(AUTHENTICATION_DATA_HOLDER.getAuthenticationData());

        // when
        boolean actualResult = cmsPsuAisServiceInternal.setScaAuthenticationData(AUTHORISATION_ID, AUTHENTICATION_DATA_HOLDER, INSTANCE_ID);

        // then
        assertThat(actualResult).isTrue();
        verify(aisConsentAuthorisationRepository, times(1)).save(expectedAuthorization);
    }

    @Test
    public void setScaAuthenticationData_authorisationNotFound() throws AuthorisationIsExpiredException {
        // given
        when(aisConsentAuthorisationRepository.findOne(any())).thenReturn(Optional.empty());

        // when
        boolean actualResult = cmsPsuAisServiceInternal.setScaAuthenticationData(AUTHORISATION_ID, AUTHENTICATION_DATA_HOLDER, INSTANCE_ID);

        // then
        assertThat(actualResult).isFalse();
        verify(aisConsentAuthorisationRepository, times(0)).save(any());
    }

    @Test
    public void setScaAuthenticationData_consentIsExpired() throws AuthorisationIsExpiredException {
        // given
        AisConsentAuthorization authorization = buildAisConsentAuthorisation();
        AisConsent consent = authorization.getConsent();
        consent.setConsentStatus(ConsentStatus.EXPIRED);

        when(aisConsentAuthorisationRepository.findOne(any())).thenReturn(Optional.of(authorization));

        // when
        boolean actualResult = cmsPsuAisServiceInternal.setScaAuthenticationData(AUTHORISATION_ID, AUTHENTICATION_DATA_HOLDER, INSTANCE_ID);

        // then
        assertThat(actualResult).isFalse();
        verify(aisConsentAuthorisationRepository, times(0)).save(any());
    }

    @Test(expected = AuthorisationIsExpiredException.class)
    public void setScaAuthenticationData_authorisationIsExpired() throws AuthorisationIsExpiredException {
        // given
        AisConsentAuthorization authorization = buildAisConsentAuthorisation();
        authorization.setAuthorisationExpirationTimestamp(LocalDateTime.now().minusDays(1).atOffset(ZoneOffset.UTC));

        when(aisConsentAuthorisationRepository.findOne(any())).thenReturn(Optional.of(authorization));

        // when
        cmsPsuAisServiceInternal.setScaAuthenticationData(AUTHORISATION_ID, AUTHENTICATION_DATA_HOLDER, INSTANCE_ID);

        // then
        verify(aisConsentAuthorisationRepository, times(0)).save(any());
    }

    @Test
    public void setScaAuthenticationData_authorisationIsReceived() throws AuthorisationIsExpiredException {
        // given
        AisConsentAuthorization authorization = buildAisConsentAuthorisation();
        authorization.setScaStatus(ScaStatus.RECEIVED);

        when(aisConsentAuthorisationRepository.findOne(any())).thenReturn(Optional.of(authorization));

        // when
        boolean actualResult = cmsPsuAisServiceInternal.setScaAuthenticationData(AUTHORISATION_ID, AUTHENTICATION_DATA_HOLDER, INSTANCE_ID);

        // then
        assertThat(actualResult).isFalse();
        verify(aisConsentAuthorisationRepository, times(0)).save(any());
    }

    private AisConsentAuthorization buildAisConsentAuthorisation() {
        return jsonReader.getObjectFromFile("json/ais_authorisation.json", AisConsentAuthorization.class);
    }
}
