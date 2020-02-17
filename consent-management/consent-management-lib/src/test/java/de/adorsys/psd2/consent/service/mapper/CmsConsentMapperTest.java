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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CmsConsentMapperTest {
    @InjectMocks
    private CmsConsentMapper cmsConsentMapper;
    @Mock
    private AuthorisationTemplateMapper authorisationTemplateMapper;
    @Mock
    private ConsentTppInformationMapper consentTppInformationMapper;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private AuthorisationMapper authorisationMapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToCmsConsent() {
        //Given
        List<AuthorisationEntity> authorisations = Collections.emptyList();
        ConsentEntity consentEntity = jsonReader.getObjectFromFile("json/service/mapper/cms-consent-mapper/consent-entity.json", ConsentEntity.class);
        CmsConsent cmsConsentExpected = jsonReader.getObjectFromFile("json/service/mapper/cms-consent-mapper/cms-consent.json", CmsConsent.class);
        when(consentTppInformationMapper.mapToConsentTppInformation(consentEntity.getTppInformation())).thenReturn(cmsConsentExpected.getTppInformation());
        when(authorisationTemplateMapper.mapToAuthorisationTemplate(consentEntity.getAuthorisationTemplate())).thenReturn(cmsConsentExpected.getAuthorisationTemplate());
        when(psuDataMapper.mapToPsuIdDataList(consentEntity.getPsuDataList())).thenReturn(cmsConsentExpected.getPsuIdDataList());
        when(authorisationMapper.mapToAuthorisations(authorisations)).thenReturn(cmsConsentExpected.getAuthorisations());
        //When
        CmsConsent cmsConsent = cmsConsentMapper.mapToCmsConsent(consentEntity, authorisations, getUsages());
        //Then
        assertEquals(cmsConsentExpected, cmsConsent);
    }

    @Test
    void mapToNewConsentEntity() {
        //Given
        ConsentEntity consentEntityExpected = jsonReader.getObjectFromFile("json/service/mapper/cms-consent-mapper/new-consent-entity.json", ConsentEntity.class);
        CmsConsent cmsConsent = jsonReader.getObjectFromFile("json/service/mapper/cms-consent-mapper/new-cms-consent.json", CmsConsent.class);
        when(authorisationTemplateMapper.mapToAuthorisationTemplateEntity(cmsConsent.getAuthorisationTemplate())).thenReturn(consentEntityExpected.getAuthorisationTemplate());
        when(consentTppInformationMapper.mapToConsentTppInformationEntity(cmsConsent.getTppInformation())).thenReturn(consentEntityExpected.getTppInformation());
        when(psuDataMapper.mapToPsuDataList(cmsConsent.getPsuIdDataList())).thenReturn(consentEntityExpected.getPsuDataList());
        //When
        ConsentEntity consentEntity = cmsConsentMapper.mapToNewConsentEntity(cmsConsent);
        consentEntityExpected.setExternalId(consentEntity.getExternalId());
        consentEntityExpected.setLastActionDate(consentEntity.getLastActionDate());
        consentEntityExpected.setRequestDateTime(consentEntity.getRequestDateTime());
        consentEntityExpected.setCreationTimestamp(consentEntity.getCreationTimestamp());
        //Then
        assertEquals(consentEntityExpected, consentEntity);
    }

    private Map<String, Integer> getUsages() {
        Map<String, Integer> usages = new HashMap<>();
        usages.put("/v1/accounts", 12);
        return usages;
    }
}
