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

package de.adorsys.psd2.xs2a.integration.builder.ais;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationTemplate;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.integration.builder.AuthorisationTemplateBuilder;
import de.adorsys.psd2.xs2a.integration.builder.PsuIdDataBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.io.IOUtils.resourceToString;

public class CmsConsentBuilder {
    private final static TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();
    private final static AuthorisationTemplate AUTHORISATION_TEMPLATE = AuthorisationTemplateBuilder.buildAuthorisationTemplate();
    private final static PsuIdData PSU_DATA = PsuIdDataBuilder.buildPsuIdData();
    private final static String AUTHORISATION_ID = UUID.randomUUID().toString();
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final ConsentDataMapper consentDataMapper = new ConsentDataMapper();

    public static CmsConsent buildCmsConsent(String jsonPath, ScaApproach scaApproach, String encryptConsentId, Xs2aObjectMapper mapper, Authorisation authorisation) throws IOException {
        CreateConsentReq consentReq = mapper.readValue(resourceToString(jsonPath, UTF_8), new TypeReference<CreateConsentReq>() {
        });
        return buildCmsConsent(consentReq, encryptConsentId, scaApproach, authorisation);
    }

    public static CmsConsent buildCmsConsent(String jsonPath, ScaApproach scaApproach, String encryptConsentId, Xs2aObjectMapper mapper) throws IOException {
        return buildCmsConsent(jsonPath, scaApproach, encryptConsentId, mapper, null);
    }

    private static CmsConsent buildCmsConsent(CreateConsentReq consentReq, String consentId, ScaApproach scaApproach, Authorisation authorisation) {
        return Optional.ofNullable(consentReq)
                   .map(cr -> {
                            AisConsentData aisConsentData = new AisConsentData(cr.getAvailableAccounts(),
                                                                               cr.getAllPsd2(),
                                                                               cr.getAvailableAccountsWithBalance(),
                                                                               cr.isCombinedServiceIndicator());

                            byte[] bytes = consentDataMapper.getBytesFromAisConsentData(aisConsentData);
                            OffsetDateTime now = OffsetDateTime.now();
                            ConsentTppInformation tppInformation = new ConsentTppInformation();
                            tppInformation.setTppRedirectPreferred(ScaApproach.REDIRECT.equals(scaApproach));
                            tppInformation.setTppInfo(TPP_INFO);

                            CmsConsent cmsConsent = new CmsConsent();
                            cmsConsent.setConsentData(bytes);
                            cmsConsent.setId(consentId);
                            cmsConsent.setRecurringIndicator(cr.isRecurringIndicator());
                            cmsConsent.setValidUntil(cr.getValidUntil());
                            cmsConsent.setFrequencyPerDay(cr.getFrequencyPerDay());
                            cmsConsent.setLastActionDate(LocalDate.now());
                            cmsConsent.setConsentStatus(ConsentStatus.RECEIVED);
                            cmsConsent.setPsuIdDataList(Collections.singletonList(PSU_DATA));
                            cmsConsent.setAuthorisationTemplate(AUTHORISATION_TEMPLATE);
                            cmsConsent.setAuthorisations(Collections.singletonList(authorisation != null ? authorisation : new Authorisation(AUTHORISATION_ID, PSU_DATA, consentId, AuthorisationType.AIS, ScaStatus.RECEIVED)));
                            cmsConsent.setUsages(Collections.emptyMap());
                            cmsConsent.setCreationTimestamp(now);
                            cmsConsent.setStatusChangeTimestamp(now);
                            cmsConsent.setTppInformation(tppInformation);

                            return cmsConsent;
                        }
                   )
                   .orElse(null);
    }
}
