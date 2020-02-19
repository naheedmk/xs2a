// TODO: 19.02.2020  https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1170
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
//package de.adorsys.psd2.consent.service;
//
//import de.adorsys.psd2.consent.api.ais.CmsConsent;
//import de.adorsys.psd2.consent.domain.account.AisConsentTransaction;
//import de.adorsys.psd2.consent.repository.AisConsentTransactionRepository;
//import de.adorsys.psd2.consent.repository.AisConsentUsageRepository;
//import de.adorsys.psd2.core.data.AccountAccess;
//import de.adorsys.psd2.core.data.ais.AisConsentData;
//import de.adorsys.psd2.core.mapper.ConsentDataMapper;
//import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
//import de.adorsys.psd2.xs2a.core.profile.AccountReference;
//import de.adorsys.xs2a.reader.JsonReader;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Spy;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.PageRequest;
//
//import java.util.Collections;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class OneOffConsentExpirationServiceTest {
//    public static final Long CONSENT_ID = 123358L;
//    public static final String RESOURCE_ID = "LGCGDC4KTx0tgnpZGYTTr8";
//
//    @InjectMocks
//    private OneOffConsentExpirationService oneOffConsentExpirationService;
//
//    @Mock
//    private AisConsentUsageRepository aisConsentUsageRepository;
//    @Mock
//    private AisConsentTransactionRepository aisConsentTransactionRepository;
//
//    @Spy
//    private ConsentDataMapper consentDataMapper = new ConsentDataMapper();
//
//    private JsonReader jsonReader = new JsonReader();
//    private AisConsentData aisConsentData;
//    private AisConsentTransaction aisConsentTransaction;
//    private CmsConsent cmsConsent;
//    private AccountReference accountReference;
//
//    @BeforeEach
//    void setUp() {
//        aisConsentData = jsonReader.getObjectFromFile("json/service/ais-consent-data.json", AisConsentData.class);
//        accountReference = jsonReader.getObjectFromFile("json/service/account-reference.json", AccountReference.class);
//        aisConsentTransaction = new AisConsentTransaction();
//        cmsConsent = new CmsConsent();
//    }
//
//    @Test
//    void isConsentExpired_multipleAccounts_partiallyUsed_shouldReturnFalse() {
//        // Given
//        cmsConsent.setConsentData(consentDataMapper.getBytesFromAisConsentData(aisConsentData));
//        aisConsentTransaction.setNumberOfTransactions(1);
//
//
//        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, PageRequest.of(0, 1)))
//            .thenReturn(Collections.singletonList(aisConsentTransaction));
//        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID)).thenReturn(0);
//
//        // When
//        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);
//
//        // Then
//        assertFalse(isExpired);
//    }
//
//    @Test
//    void isConsentExpired_multipleAccounts_fullyUsed_shouldReturnTrue() {
//        // Given
//        cmsConsent.setConsentData(consentDataMapper.getBytesFromAisConsentData(aisConsentData));
//        aisConsentTransaction.setNumberOfTransactions(1);
//
//        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, PageRequest.of(0, 1)))
//            .thenReturn(Collections.singletonList(aisConsentTransaction));
//        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID)).thenReturn(1);
//
//        // When
//        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);
//
//        // Then
//        assertTrue(isExpired);
//    }
//
//    @Test
//    void isConsentExpired_allAvailableAccounts_shouldReturnTrue() {
//        // Given
//        AccountAccess aspspAccountAccess = aisConsentData.getAspspAccountAccess();
//        cmsConsent.setConsentData(consentDataMapper.getBytesFromAisConsentData(new AisConsentData(aisConsentData.getTppAccountAccess(),
//                                                                                                  new AccountAccess(aspspAccountAccess.getAccounts(),
//                                                                                                                    aspspAccountAccess.getBalances(),
//                                                                                                                    aspspAccountAccess.getTransactions(),
//                                                                                                                    AccountAccessType.ALL_ACCOUNTS,
//                                                                                                                    aspspAccountAccess.getAllPsd2(),
//                                                                                                                    aspspAccountAccess.getAvailableAccountsWithBalance(),
//                                                                                                                    aspspAccountAccess.getAdditionalInformationAccess()),
//                                                                                                  aisConsentData.isCombinedServiceIndicator())));
//
//        // When
//        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);
//
//        // Then
//        assertTrue(isExpired);
//    }
//
//    @Test
//    void isConsentExpired_bankOffered_shouldReturnFalse() {
//        // Given
//        cmsConsent.setConsentData(consentDataMapper.getBytesFromAisConsentData(new AisConsentData(aisConsentData.getTppAccountAccess(),
//                                                                                                  new AccountAccess(Collections.emptyList(),
//                                                                                                                    Collections.emptyList(),
//                                                                                                                    Collections.emptyList(),
//                                                                                                                    null,
//                                                                                                                    null,
//                                                                                                                    null,
//                                                                                                                    null),
//                                                                                                  aisConsentData.isCombinedServiceIndicator())));
//
//        // When
//        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);
//
//        // Then
//        assertFalse(isExpired);
//    }
//
//    @Test
//    void isConsentExpired_globalFullAccesses_notUsed_shouldReturnFalse() {
//        // Given
//        AccountAccess aspspAccountAccess = aisConsentData.getAspspAccountAccess();
//        cmsConsent.setConsentData(consentDataMapper.getBytesFromAisConsentData(new AisConsentData(aisConsentData.getTppAccountAccess(),
//                                                                                                  new AccountAccess(aspspAccountAccess.getAccounts(),
//                                                                                                                    aspspAccountAccess.getBalances(),
//                                                                                                                    aspspAccountAccess.getTransactions(),
//                                                                                                                    aspspAccountAccess.getAvailableAccounts(),
//                                                                                                                    AccountAccessType.ALL_ACCOUNTS,
//                                                                                                                    aspspAccountAccess.getAvailableAccountsWithBalance(),
//                                                                                                                    aspspAccountAccess.getAdditionalInformationAccess()),
//                                                                                                  aisConsentData.isCombinedServiceIndicator())));
//        aisConsentTransaction.setNumberOfTransactions(1);
//
//        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, PageRequest.of(0, 1)))
//            .thenReturn(Collections.singletonList(aisConsentTransaction));
//        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID)).thenReturn(0);
//
//        // When
//        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);
//
//        // Then
//        assertFalse(isExpired);
//    }
//
//    @Test
//    void isConsentExpired_globalFullAccesses_fullyUsed_shouldReturnTrue() {
//        // Given
//        AccountAccess aspspAccountAccess = aisConsentData.getAspspAccountAccess();
//        cmsConsent.setConsentData(consentDataMapper.getBytesFromAisConsentData(new AisConsentData(aisConsentData.getTppAccountAccess(),
//                                                                                                  new AccountAccess(aspspAccountAccess.getAccounts(),
//                                                                                                                    aspspAccountAccess.getBalances(),
//                                                                                                                    aspspAccountAccess.getTransactions(),
//                                                                                                                    aspspAccountAccess.getAvailableAccounts(),
//                                                                                                                    AccountAccessType.ALL_ACCOUNTS,
//                                                                                                                    aspspAccountAccess.getAvailableAccountsWithBalance(),
//                                                                                                                    aspspAccountAccess.getAdditionalInformationAccess()),
//                                                                                                  aisConsentData.isCombinedServiceIndicator())));
//        aisConsentTransaction.setNumberOfTransactions(1);
//
//        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, PageRequest.of(0, 1)))
//            .thenReturn(Collections.singletonList(aisConsentTransaction));
//        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID)).thenReturn(1);
//
//        // When
//        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);
//
//        // Then
//        assertTrue(isExpired);
//    }
//
//    @Test
//    void isConsentExpired_dedicatedWithBalances_partiallyUsed_shouldReturnFalse() {
//        // Given
//        AccountAccess aspspAccountAccess = aisConsentData.getAspspAccountAccess();
//        cmsConsent.setConsentData(consentDataMapper.getBytesFromAisConsentData(new AisConsentData(aisConsentData.getTppAccountAccess(),
//                                                                                                  new AccountAccess(Collections.emptyList(),
//                                                                                                                    Collections.singletonList(accountReference),
//                                                                                                                    Collections.emptyList(),
//                                                                                                                    aspspAccountAccess.getAvailableAccounts(),
//                                                                                                                    aspspAccountAccess.getAllPsd2(),
//                                                                                                                    aspspAccountAccess.getAvailableAccountsWithBalance(),
//                                                                                                                    aspspAccountAccess.getAdditionalInformationAccess()),
//                                                                                                  aisConsentData.isCombinedServiceIndicator())));
//        aisConsentTransaction.setNumberOfTransactions(2);
//
//        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, PageRequest.of(0, 1)))
//            .thenReturn(Collections.singletonList(aisConsentTransaction));
//        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID)).thenReturn(1);
//
//        // When
//        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);
//
//        // Then
//        assertFalse(isExpired);
//    }
//
//    @Test
//    void isConsentExpired_dedicatedWithBalances_fullyUsed_shouldReturnTrue() {
//        // Given
//        AccountAccess aspspAccountAccess = aisConsentData.getAspspAccountAccess();
//        cmsConsent.setConsentData(consentDataMapper.getBytesFromAisConsentData(new AisConsentData(aisConsentData.getTppAccountAccess(),
//                                                                                                  new AccountAccess(Collections.emptyList(),
//                                                                                                                    Collections.singletonList(accountReference),
//                                                                                                                    Collections.emptyList(),
//                                                                                                                    aspspAccountAccess.getAvailableAccounts(),
//                                                                                                                    aspspAccountAccess.getAllPsd2(),
//                                                                                                                    aspspAccountAccess.getAvailableAccountsWithBalance(),
//                                                                                                                    aspspAccountAccess.getAdditionalInformationAccess()),
//                                                                                                  aisConsentData.isCombinedServiceIndicator())));
//        aisConsentTransaction.setNumberOfTransactions(2);
//
//        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, PageRequest.of(0, 1)))
//            .thenReturn(Collections.singletonList(aisConsentTransaction));
//        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID)).thenReturn(2);
//
//        // When
//        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);
//
//        // Then
//        assertTrue(isExpired);
//    }
//
//    @Test
//    void isConsentExpired_dedicatedWithTransactions_partiallyUsed_shouldReturnFalse() {
//        // Given
//        AccountAccess aspspAccountAccess = aisConsentData.getAspspAccountAccess();
//        cmsConsent.setConsentData(consentDataMapper.getBytesFromAisConsentData(new AisConsentData(aisConsentData.getTppAccountAccess(),
//                                                                                                  new AccountAccess(Collections.emptyList(),
//                                                                                                                    Collections.emptyList(),
//                                                                                                                    Collections.singletonList(accountReference),
//                                                                                                                    aspspAccountAccess.getAvailableAccounts(),
//                                                                                                                    aspspAccountAccess.getAllPsd2(),
//                                                                                                                    aspspAccountAccess.getAvailableAccountsWithBalance(),
//                                                                                                                    aspspAccountAccess.getAdditionalInformationAccess()),
//                                                                                                  aisConsentData.isCombinedServiceIndicator())));
//        aisConsentTransaction.setNumberOfTransactions(2);
//
//        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, PageRequest.of(0, 1)))
//            .thenReturn(Collections.singletonList(aisConsentTransaction));
//        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID)).thenReturn(1);
//
//        // When
//        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);
//
//        // Then
//        assertFalse(isExpired);
//    }
//
//    @Test
//    void isConsentExpired_dedicatedWithTransactions_fullyUsed_shouldReturnTrue() {
//        // Given
//        AccountAccess aspspAccountAccess = aisConsentData.getAspspAccountAccess();
//        cmsConsent.setConsentData(consentDataMapper.getBytesFromAisConsentData(new AisConsentData(aisConsentData.getTppAccountAccess(),
//                                                                                                  new AccountAccess(Collections.emptyList(),
//                                                                                                                    Collections.emptyList(),
//                                                                                                                    Collections.singletonList(accountReference),
//                                                                                                                    aspspAccountAccess.getAvailableAccounts(),
//                                                                                                                    aspspAccountAccess.getAllPsd2(),
//                                                                                                                    aspspAccountAccess.getAvailableAccountsWithBalance(),
//                                                                                                                    aspspAccountAccess.getAdditionalInformationAccess()),
//                                                                                                  aisConsentData.isCombinedServiceIndicator())));
//        aisConsentTransaction.setNumberOfTransactions(2);
//
//        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, PageRequest.of(0, 1)))
//            .thenReturn(Collections.singletonList(aisConsentTransaction));
//        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID)).thenReturn(4);
//
//        // When
//        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);
//
//        // Then
//        assertTrue(isExpired);
//    }
//
//}
