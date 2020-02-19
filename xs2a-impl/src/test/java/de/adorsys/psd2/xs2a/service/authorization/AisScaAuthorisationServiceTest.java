// TODO: 19.02.2020 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1170
///*
// * Copyright 2018-2020 adorsys GmbH & Co KG
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package de.adorsys.psd2.xs2a.service.authorization;
//
//import de.adorsys.psd2.core.data.AccountAccess;
//import de.adorsys.psd2.core.data.ais.AisConsent;
//import de.adorsys.psd2.core.data.ais.AisConsentData;
//import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
//import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
//import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
//import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
//import de.adorsys.psd2.xs2a.core.profile.AccountReference;
//import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
//import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
//import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
//import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.time.OffsetDateTime;
//import java.time.ZoneOffset;
//import java.util.Collections;
//import java.util.Currency;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class AisScaAuthorisationServiceTest {
//    @InjectMocks
//    private AisScaAuthorisationService aisScaAuthorisationService;
//    @Mock
//    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
//
//    @Test
//    void isOneFactorAuthorisation_AllAvailableConsent_OneAccessTypeTrue_ScaRequiredFalse() {
//        //Given
//        when(aspspProfileServiceWrapper.isScaByOneTimeAvailableAccountsConsentRequired()).thenReturn(false);
//        AisConsent consent = buildAvailableAccountConsent(true);
//
//        //When
//        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);
//
//        //Then
//        assertTrue(oneFactorAuthorisation);
//    }
//
//    @Test
//    void isOneFactorAuthorisation_AllAvailableConsent_OneAccessTypeTrue_ScaRequiredTrue() {
//        //Given
//        when(aspspProfileServiceWrapper.isScaByOneTimeAvailableAccountsConsentRequired()).thenReturn(true);
//        AisConsent consent = buildAvailableAccountConsent(true);
//
//        //When
//        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);
//
//        //Then
//        assertFalse(oneFactorAuthorisation);
//    }
//
//    @Test
//    void isOneFactorAuthorisation_AllAvailableConsent_OneAccessTypeFalse() {
//        //Given
//        AisConsent consent = buildAvailableAccountConsent(false);
//
//        //When
//        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);
//
//        //Then
//        assertFalse(oneFactorAuthorisation);
//    }
//
//    @Test
//    void isOneFactorAuthorisation_GlobalConsent_OneAccessTypeTrue_ScaRequiredFalse() {
//        //Given
//        when(aspspProfileServiceWrapper.isScaByOneTimeGlobalConsentRequired()).thenReturn(false);
//        AisConsent consent = buildGlobalConsent(true);
//
//        //When
//        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);
//
//        //Then
//        assertTrue(oneFactorAuthorisation);
//    }
//
//    @Test
//    void isOneFactorAuthorisation_GlobalConsent_OneAccessTypeTrue_ScaRequiredTrue() {
//        //Given
//        when(aspspProfileServiceWrapper.isScaByOneTimeGlobalConsentRequired()).thenReturn(true);
//        AisConsent consent = buildGlobalConsent(true);
//
//        //When
//        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);
//
//        //Then
//        assertFalse(oneFactorAuthorisation);
//    }
//
//    @Test
//    void isOneFactorAuthorisation_GlobalConsent_OneAccessTypeFalse() {
//        //Given
//        AisConsent consent = buildGlobalConsent(false);
//
//        //When
//        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);
//
//        //Then
//        assertFalse(oneFactorAuthorisation);
//    }
//
//    @Test
//    void isOneFactorAuthorisation_BankOfferedConsent_OneAccessTypeTrue() {
//        //Given
//        AisConsent consent = buildBankOfferedConsent(true);
//
//        //When
//        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);
//
//        //Then
//        assertFalse(oneFactorAuthorisation);
//    }
//
//    @Test
//    void isOneFactorAuthorisation_BankOfferedConsent_OneAccessTypeFalse() {
//        //Given
//        AisConsent consent = buildBankOfferedConsent(false);
//
//        //When
//        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);
//
//        //Then
//        assertFalse(oneFactorAuthorisation);
//    }
//
//
//    @Test
//    void isOneFactorAuthorisation_DedicatedConsent_OneAccessTypeTrue_ScaRequiredTrue() {
//        //Given
//        AisConsent consent = buildDedicatedConsent(true);
//
//        //When
//        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);
//        //Then
//        assertFalse(oneFactorAuthorisation);
//    }
//
//    @Test
//    void isOneFactorAuthorisation_DedicatedConsent_OneAccessTypeFalse_ScaRequiredTrue() {
//        //Given
//        AisConsent consent = buildDedicatedConsent(false);
//
//        //When
//        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);
//        //Then
//        assertFalse(oneFactorAuthorisation);
//    }
//
//    @Test
//    void isOneFactorAuthorisation_DedicatedConsent_OneAccessTypeTrue_ScaRequiredFalse() {
//        //Given
//        AisConsent consent = buildDedicatedConsent(true);
//
//        //When
//        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);
//        //Then
//        assertFalse(oneFactorAuthorisation);
//    }
//
//    private AisConsent buildAvailableAccountConsent(boolean oneAccessType) {
//        AccountAccess accountAccess = new AccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), AccountAccessType.ALL_ACCOUNTS, null, null, null);
//        return buildConsent(accountAccess, oneAccessType, AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS);
//    }
//
//    private AisConsent buildGlobalConsent(boolean oneAccessType) {
//        AccountAccess accountAccess = new AccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, AccountAccessType.ALL_ACCOUNTS, null, null);
//        return buildConsent(accountAccess, oneAccessType, AisConsentRequestType.GLOBAL);
//    }
//
//    private AisConsent buildBankOfferedConsent(boolean oneAccessType) {
//        AccountAccess accountAccess = new AccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null, null, null);
//        return buildConsent(accountAccess, oneAccessType, AisConsentRequestType.BANK_OFFERED);
//    }
//
//    private AisConsent buildDedicatedConsent(boolean oneAccessType) {
//        AccountAccess accountAccess = new AccountAccess(Collections.singletonList(new AccountReference(AccountReferenceType.IBAN, "DE86500105176716126648", Currency.getInstance("EUR"))), Collections.emptyList(), Collections.emptyList(), null, null, null, null);
//        return buildConsent(accountAccess, oneAccessType, AisConsentRequestType.DEDICATED_ACCOUNTS);
//    }
//
//    private AisConsent buildConsent(AccountAccess accountAccess, boolean oneAccessType, AisConsentRequestType consentRequestType) {
//        return createConsent(accountAccess, LocalDate.of(2019, 9, 19),  OffsetDateTime.of(2019, 9, 19, 12, 0, 0, 0, ZoneOffset.UTC), oneAccessType);
//    }
//
//    private static AisConsent createConsent(AccountAccess access, LocalDate validUntil, OffsetDateTime statusChangeTimeStamp, boolean oneAccessType ) {
//        AisConsent aisConsent = new AisConsent();
//        aisConsent.setConsentData(buildAisConsentData(access));
//        aisConsent.setId("some isd");
//        aisConsent.setValidUntil(validUntil);
//        aisConsent.setFrequencyPerDay( oneAccessType ? 1 : 2 );
//        aisConsent.setConsentStatus(ConsentStatus.VALID);
//        aisConsent.setAuthorisations(Collections.emptyList());
//        aisConsent.setConsentTppInformation(buildConsentTppInformation());
//        aisConsent.setStatusChangeTimestamp(statusChangeTimeStamp);
//        aisConsent.setUsages(Collections.emptyMap());
//        aisConsent.setStatusChangeTimestamp(OffsetDateTime.now());
//        aisConsent.setRecurringIndicator( !oneAccessType );
//        return aisConsent;
//    }
//
//    private static AisConsentData buildAisConsentData(AccountAccess access) {
//        return new AisConsentData(access, access, false);
//    }
//
//    private static ConsentTppInformation buildConsentTppInformation() {
//        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
//        consentTppInformation.setTppInfo(new TppInfo());
//        return consentTppInformation;
//    }
//}
