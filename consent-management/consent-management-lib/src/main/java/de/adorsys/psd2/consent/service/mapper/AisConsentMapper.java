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

import de.adorsys.psd2.consent.api.ais.AisAccountAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountConsentAuthorisation;
import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.service.AisConsentUsageService;
import de.adorsys.psd2.core.data.ais.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.authorisation.AccountConsentAuthorization;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AisConsentMapper {
    private final PsuDataMapper psuDataMapper;
    private final TppInfoMapper tppInfoMapper;
    private final AisConsentUsageService aisConsentUsageService;
    private final AuthorisationTemplateMapper authorisationTemplateMapper;
    private final ConsentDataMapper consentDataMapper;
    private final ConsentTppInformationMapper consentTppInformationMapper;

    private AisAccountAccess getAvailableAccess(ConsentEntity consent) {
        AisAccountAccess tppAccountAccess = mapToAisAccountAccess(consent);
        AisAccountAccess aspspAccountAccess = mapToAspspAisAccountAccess(consent);

        if (tppAccountAccess.getAllPsd2() != null
                || !aspspAccountAccess.isNotEmpty()) {
            return tppAccountAccess;
        }

        return aspspAccountAccess;
    }

    public CmsAisAccountConsent mapToCmsAisAccountConsent(ConsentEntity consent, List<AuthorisationEntity> authorisations) {
        AisAccountAccess chosenAccess = getAvailableAccess(consent);

        Map<String, Integer> usageCounterMap = aisConsentUsageService.getUsageCounterMap(consent);
        AisConsentData aisConsentData = consentDataMapper.mapToAisConsentData(consent.getData());

        return new CmsAisAccountConsent(
            consent.getExternalId(),
            chosenAccess,
            consent.isRecurringIndicator(),
            consent.getValidUntil(),
            consent.getExpireDate(),
            consent.getFrequencyPerDay(),
            consent.getLastActionDate(),
            consent.getConsentStatus(),
            CollectionUtils.isNotEmpty(aisConsentData.getTppAccountAccess().getBalances()),
            consent.getTppInformation().isTppRedirectPreferred(),
            aisConsentData.getConsentRequestType(),
            psuDataMapper.mapToPsuIdDataList(consent.getPsuDataList()),
            tppInfoMapper.mapToTppInfo(consent.getTppInformation().getTppInfo()),
            authorisationTemplateMapper.mapToAuthorisationTemplate(consent.getAuthorisationTemplate()),
            consent.isMultilevelScaRequired(),
            mapToAisAccountConsentAuthorisation(authorisations),
            usageCounterMap,
            consent.getCreationTimestamp(),
            consent.getStatusChangeTimestamp());
    }

    public AisConsent mapToAisConsent(ConsentEntity entity, List<AuthorisationEntity> authorisations) {
        AisConsentData aisConsentData = consentDataMapper.mapToAisConsentData(entity.getData());

        Map<String, Integer> usageCounterMap = aisConsentUsageService.getUsageCounterMap(entity);

        return new AisConsent(aisConsentData,
                              entity.getExternalId(),
                              entity.getInternalRequestId(),
                              entity.getConsentStatus(),
                              entity.getFrequencyPerDay(),
                              entity.isRecurringIndicator(),
                              entity.isMultilevelScaRequired(),
                              entity.getValidUntil(),
                              entity.getExpireDate(),
                              entity.getLastActionDate(),
                              entity.getCreationTimestamp(),
                              entity.getStatusChangeTimestamp(),
                              consentTppInformationMapper.mapToConsentTppInformation(entity.getTppInformation()),
                              authorisationTemplateMapper.mapToAuthorisationTemplate(entity.getAuthorisationTemplate()),
                              psuDataMapper.mapToPsuIdDataList(entity.getPsuDataList()),
                              mapToAccountConsentAuthorisations(authorisations),
                              usageCounterMap);
    }

    public AccountAccess mapToAccountAccess(AisAccountAccess accountAccess) {

        return new AccountAccess(ListUtils.emptyIfNull(accountAccess.getAccounts()),
                                 ListUtils.emptyIfNull(accountAccess.getBalances()),
                                 ListUtils.emptyIfNull(accountAccess.getTransactions()),
                                 Optional.ofNullable(accountAccess.getAvailableAccounts())
                                     .flatMap(AccountAccessType::getByDescription)
                                     .orElse(null),

                                 Optional.ofNullable(accountAccess.getAllPsd2())
                                     .flatMap(AccountAccessType::getByDescription)
                                     .orElse(null),

                                 Optional.ofNullable(accountAccess.getAvailableAccountsWithBalance())
                                     .flatMap(AccountAccessType::getByDescription)
                                     .orElse(null),

                                 accountAccess.getAccountAdditionalInformationAccess());
    }

    private AisAccountAccess mapToAisAccountAccess(ConsentEntity consent) {
        AisConsentData aisConsentData = consentDataMapper.mapToAisConsentData(consent.getData());
        AccountAccess tppAccesses = aisConsentData.getTppAccountAccess();

        return new AisAccountAccess(tppAccesses.getAccounts(),
                                    tppAccesses.getBalances(),
                                    tppAccesses.getTransactions(),
                                    getAccessType(tppAccesses.getAvailableAccounts()),
                                    getAccessType(tppAccesses.getAllPsd2()),
                                    getAccessType(tppAccesses.getAvailableAccountsWithBalance()),
                                    tppAccesses.getAdditionalInformationAccess()
        );
    }

    private AisAccountAccess mapToAspspAisAccountAccess(ConsentEntity consent) {
        AisConsentData aisConsentData = consentDataMapper.mapToAisConsentData(consent.getData());
        AccountAccess aspspAccesses = aisConsentData.getAspspAccountAccess();

        return new AisAccountAccess(aspspAccesses.getAccounts(),
                                    aspspAccesses.getBalances(),
                                    aspspAccesses.getTransactions(),
                                    getAccessType(aspspAccesses.getAvailableAccounts()),
                                    getAccessType(aspspAccesses.getAllPsd2()),
                                    getAccessType(aspspAccesses.getAvailableAccountsWithBalance()),
                                    aspspAccesses.getAdditionalInformationAccess()
        );
    }

    private String getAccessType(AccountAccessType type) {
        return Optional.ofNullable(type)
                   .map(Enum::name)
                   .orElse(null);
    }


    private List<AisAccountConsentAuthorisation> mapToAisAccountConsentAuthorisation(List<AuthorisationEntity> aisConsentAuthorisations) {
        if (CollectionUtils.isEmpty(aisConsentAuthorisations)) {
            return Collections.emptyList();
        }

        return aisConsentAuthorisations.stream()
                   .map(this::mapToAisAccountConsentAuthorisation)
                   .collect(Collectors.toList());
    }

    private List<AccountConsentAuthorization> mapToAccountConsentAuthorisations(List<AuthorisationEntity> aisConsentAuthorisations) {
        if (CollectionUtils.isEmpty(aisConsentAuthorisations)) {
            return Collections.emptyList();
        }

        return aisConsentAuthorisations.stream()
                   .map(this::mapToAccountConsentAuthorisation)
                   .collect(Collectors.toList());
    }

    private AisAccountConsentAuthorisation mapToAisAccountConsentAuthorisation(AuthorisationEntity aisConsentAuthorisation) {
        return Optional.ofNullable(aisConsentAuthorisation)
                   .map(auth -> new AisAccountConsentAuthorisation(auth.getExternalId(),
                                                                   psuDataMapper.mapToPsuIdData(auth.getPsuData()),
                                                                   auth.getScaStatus()))
                   .orElse(null);
    }

    private AccountConsentAuthorization mapToAccountConsentAuthorisation(AuthorisationEntity aisConsentAuthorisation) {
        return Optional.ofNullable(aisConsentAuthorisation)
                   .map(auth -> {
                       AccountConsentAuthorization authorisation = new AccountConsentAuthorization();

                       authorisation.setId(auth.getExternalId());
                       authorisation.setConsentId(auth.getParentExternalId());
                       authorisation.setPsuIdData(psuDataMapper.mapToPsuIdData(auth.getPsuData()));
                       authorisation.setScaStatus(auth.getScaStatus());
                       authorisation.setPassword(null);
                       authorisation.setChosenScaApproach(auth.getScaApproach());
                       authorisation.setAuthenticationMethodId(auth.getAuthenticationMethodId());
                       authorisation.setScaAuthenticationData(auth.getScaAuthenticationData());

                       return authorisation;
                   })
                   .orElse(null);
    }

}
