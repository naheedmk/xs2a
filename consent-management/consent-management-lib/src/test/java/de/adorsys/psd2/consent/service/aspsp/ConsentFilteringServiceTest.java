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
//package de.adorsys.psd2.consent.service.aspsp;
//
//import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
//import de.adorsys.psd2.core.data.AccountAccess;
//import de.adorsys.psd2.core.data.ais.AisConsentData;
//import de.adorsys.psd2.core.mapper.ConsentDataMapper;
//import de.adorsys.psd2.xs2a.core.profile.AccountReference;
//import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Currency;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class ConsentFilteringServiceTest {
//    @InjectMocks
//    private ConsentFilteringService consentFilteringService;
//    @Mock
//    private ConsentDataMapper consentDataMapper;
//
//    private static final String ASPSP_ACCOUNT_ID_1 = "aspsp account id 1";
//    private static final String ASPSP_ACCOUNT_ID_2 = "aspsp account id 2";
//    private static final String ASPSP_ACCOUNT_ID_3 = "aspsp account id 3";
//
//    private static final byte[] CONSENT_BYTES_1 = {1};
//    private static final byte[] CONSENT_BYTES_2 = {2};
//    private static final byte[] CONSENT_BYTES_3 = {3};
//
//    @Test
//    void filterAisConsentsByAspspAccountId_emptyAccountId_shouldReturnInputEntities() {
//        List<ConsentEntity> consentEntitiesInput = Arrays.asList(buildConsentEntity(CONSENT_BYTES_1), buildConsentEntity(CONSENT_BYTES_2), buildConsentEntity(CONSENT_BYTES_3));
//
//        List<ConsentEntity> consentEntities = consentFilteringService.filterAisConsentsByAspspAccountId(consentEntitiesInput, null);
//
//        assertEquals(consentEntitiesInput, consentEntities);
//    }
//
//    @Test
//    void filterAisConsentsByAspspAccountId_success_withoutAdditionalInformationAccesses() {
//        List<ConsentEntity> consentEntitiesInput = Arrays.asList(buildConsentEntity(CONSENT_BYTES_1),
//                                                                 buildConsentEntity(CONSENT_BYTES_2),
//                                                                 buildConsentEntity(CONSENT_BYTES_3));
//        List<AisConsentData> aisConsentDatas = Arrays.asList(buildAisConsentData(ASPSP_ACCOUNT_ID_1, false),
//                                                             buildAisConsentData(ASPSP_ACCOUNT_ID_2, false),
//                                                             buildAisConsentData(ASPSP_ACCOUNT_ID_3, false));
//
//        for (int i = 0; i < consentEntitiesInput.size(); i++) {
//            AisConsentData aisConsentData = aisConsentDatas.get(i);
//            when(consentDataMapper.mapToAisConsentData(consentEntitiesInput.get(i).getData())).thenReturn(aisConsentData);
//        }
//
//        List<ConsentEntity> consentEntities = consentFilteringService.filterAisConsentsByAspspAccountId(consentEntitiesInput, ASPSP_ACCOUNT_ID_2);
//
//        assertEquals(1, consentEntities.size());
//        assertEquals(consentEntitiesInput.get(1), consentEntities.get(0));
//    }
//
//    @Test
//    void filterAisConsentsByAspspAccountId_success_withAdditionalInformationAccesses() {
//        List<ConsentEntity> consentEntitiesInput = Arrays.asList(buildConsentEntity(CONSENT_BYTES_1),
//                                                                 buildConsentEntity(CONSENT_BYTES_2),
//                                                                 buildConsentEntity(CONSENT_BYTES_3));
//        List<AisConsentData> aisConsentDatas = Arrays.asList(buildAisConsentData(ASPSP_ACCOUNT_ID_1, true),
//                                                             buildAisConsentData(ASPSP_ACCOUNT_ID_2, true),
//                                                             buildAisConsentData(ASPSP_ACCOUNT_ID_3, true));
//
//        for (int i = 0; i < consentEntitiesInput.size(); i++) {
//            AisConsentData aisConsentData = aisConsentDatas.get(i);
//            when(consentDataMapper.mapToAisConsentData(consentEntitiesInput.get(i).getData())).thenReturn(aisConsentData);
//        }
//
//        List<ConsentEntity> consentEntities = consentFilteringService.filterAisConsentsByAspspAccountId(consentEntitiesInput, ASPSP_ACCOUNT_ID_2);
//
//        assertEquals(1, consentEntities.size());
//        assertEquals(consentEntitiesInput.get(1), consentEntities.get(0));
//    }
//
//
//    private ConsentEntity buildConsentEntity(byte[] data) {
//        ConsentEntity consentEntity = new ConsentEntity();
//        consentEntity.setData(data);
//        return consentEntity;
//    }
//
//    private AccountReference buildAccountReference(String aspspAccountId) {
//        return new AccountReference(aspspAccountId, null, null, null, null, null, null, Currency.getInstance("EUR"));
//    }
//
//    private List<AccountReference> buildAccountReferences(String aspspAccountId) {
//        return Collections.singletonList(buildAccountReference(aspspAccountId));
//    }
//
//    private AisConsentData buildAisConsentData(String aspspAccountId, boolean withAdditionalInformation) {
//        return new AisConsentData(
//            buildAccountAccesses(buildAccountReferences(aspspAccountId), withAdditionalInformation),
//            buildAccountAccesses(buildAccountReferences(aspspAccountId), withAdditionalInformation),
//            false
//        );
//    }
//
//    private AccountAccess buildAccountAccesses(List<AccountReference> accountReferences, boolean withAdditionalInformation) {
//        return new AccountAccess(
//            accountReferences,
//            accountReferences,
//            accountReferences,
//            null,
//            null,
//            null,
//            withAdditionalInformation ? new AdditionalInformationAccess(accountReferences) : null
//        );
//    }
//}
