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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.account.AisConsentAction;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AisConsentActionRepository;
import de.adorsys.psd2.consent.repository.AisConsentVerifyingRepository;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.service.mapper.AccessMapper;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.CmsConsentMapper;
import de.adorsys.psd2.core.data.ais.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.psd2.consent.api.CmsError.LOGICAL_ERROR;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.EXPIRED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AisConsentServiceInternal implements AisConsentService {

    private final ConsentJpaRepository consentJpaRepository;
    private final AisConsentVerifyingRepository aisConsentRepository;
    private final AisConsentActionRepository aisConsentActionRepository;
    private final AuthorisationRepository authorisationRepository;
    private final AisConsentMapper consentMapper;
    private final AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;
    private final AisConsentUsageService aisConsentUsageService;
    private final OneOffConsentExpirationService oneOffConsentExpirationService;
    private final CmsConsentMapper cmsConsentMapper;
    private final ConsentDataMapper consentDataMapper;
    private final AccessMapper accessMapper;

    /**
     * Saves information about consent usage and consent's sub-resources usage.
     *
     * @param request {@link AisConsentActionRequest} needed parameters for logging usage AIS consent
     */
    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<CmsResponse.VoidResponse> checkConsentAndSaveActionLog(AisConsentActionRequest request) throws WrongChecksumException {
        Optional<ConsentEntity> consentOpt = getActualAisConsent(request.getConsentId());
        if (consentOpt.isPresent()) {
            ConsentEntity consent = consentOpt.get();
            aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consent);
            checkAndUpdateOnExpiration(consent);
            // In this method sonar claims that NPE is possible:
            // https://rules.sonarsource.com/java/RSPEC-3655
            // but we have isPresent in the code before.
            updateAisConsentUsage(consent, request); //NOSONAR
            logConsentAction(consent.getExternalId(), resolveConsentActionStatus(request, consent), request.getTppId()); //NOSONAR
        }

        return CmsResponse.<CmsResponse.VoidResponse>builder()
                   .payload(CmsResponse.voidResponse())
                   .build();
    }

    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<CmsConsent> updateAspspAccountAccess(String consentId, AisAccountAccessInfo request) throws WrongChecksumException {
        Optional<ConsentEntity> consentOptional = aisConsentRepository.getActualAisConsent(consentId);

        if (!consentOptional.isPresent()) {
            log.info("Consent ID [{}]. Update aspsp account access with response failed, because consent not found",
                     consentId);
            return CmsResponse.<CmsConsent>builder()
                       .error(LOGICAL_ERROR)
                       .build();
        }

        ConsentEntity consentEntity = consentOptional.get();
        ConsentEntity updatedConsent = updateConsentAccess(consentEntity, request);
        ConsentEntity savedConsent = aisConsentRepository.verifyAndUpdate(updatedConsent);
        CmsConsent cmsConsent = mapToCmsConsent(savedConsent);

        return CmsResponse.<CmsConsent>builder()
                   .payload(cmsConsent)
                   .build();
    }

    private CmsConsent mapToCmsConsent(ConsentEntity consent) {
        List<AuthorisationEntity> authorisations = authorisationRepository.findAllByParentExternalIdAndAuthorisationType(consent.getExternalId(), AuthorisationType.AIS);
        Map<String, Integer> usageCounterMap = aisConsentUsageService.getUsageCounterMap(consent);
        return cmsConsentMapper.mapToCmsConsent(consent, authorisations, usageCounterMap);
    }

    private ConsentEntity updateConsentAccess(ConsentEntity consentEntity, AisAccountAccessInfo request) {
        AisConsentData aisConsentData = consentDataMapper.mapToAisConsentData(consentEntity.getData());
        AccountAccess existingAccess = aisConsentData.getAspspAccountAccess();
        AccountAccess requestedAccess = accessMapper.mapToAccountAccess(request);
        AccountAccess updatedAccesses = updateAccountReferencesInAccess(existingAccess, requestedAccess);

        AisConsentData updatedAisConsentData = new AisConsentData(aisConsentData.getTppAccountAccess(),
                                                                  updatedAccesses,
                                                                  aisConsentData.isCombinedServiceIndicator());
        byte[] updatedConsentData = consentDataMapper.getBytesFromAisConsentData(updatedAisConsentData);
        consentEntity.setData(updatedConsentData);

        return consentEntity;
    }

    private void updateAisConsentUsage(ConsentEntity consent, AisConsentActionRequest request) throws WrongChecksumException {
        if (!request.isUpdateUsage()) {
            return;
        }
        aisConsentUsageService.incrementUsage(consent, request);

        CmsConsent cmsConsent = mapToCmsConsent(consent);

        if (!consent.isRecurringIndicator() && consent.getFrequencyPerDay() == 1
                && oneOffConsentExpirationService.isConsentExpired(consent, cmsConsent)) {
            consent.setConsentStatus(EXPIRED);
        }

        consent.setLastActionDate(LocalDate.now());

        aisConsentRepository.verifyAndSave(consent);
    }

    private ActionStatus resolveConsentActionStatus(AisConsentActionRequest request, ConsentEntity consent) {

        if (consent == null) {
            log.info("Consent ID: [{}]. Consent action status resolver received null consent",
                     request.getConsentId());
            return ActionStatus.BAD_PAYLOAD;
        }
        return request.getActionStatus();
    }

    private void logConsentAction(String requestedConsentId, ActionStatus actionStatus, String tppId) {
        AisConsentAction action = new AisConsentAction();
        action.setActionStatus(actionStatus);
        action.setRequestedConsentId(requestedConsentId);
        action.setTppId(tppId);
        action.setRequestDate(LocalDate.now());
        aisConsentActionRepository.save(action);
    }

    private Optional<ConsentEntity> getActualAisConsent(String consentId) {
        return consentJpaRepository.findByExternalId(consentId)
                   .filter(c -> !c.getConsentStatus().isFinalisedStatus());
    }

    private ConsentEntity checkAndUpdateOnExpiration(ConsentEntity consent) {
        if (consent != null && consent.shouldConsentBeExpired()) {
            return aisConsentConfirmationExpirationService.expireConsent(consent);
        }

        return consent;
    }

    private AccountAccess updateAccountReferencesInAccess(AccountAccess existingAccess, AccountAccess requestedAccess) {
        if (hasNoAccountReferences(existingAccess)) {
            return requestedAccess;
        }

        List<AccountReference> updatedAccounts = existingAccess.getAccounts().stream()
                                                     .map(ref -> updateAccountReference(ref, requestedAccess.getAccounts())).collect(Collectors.toList());
        List<AccountReference> updatedBalances = existingAccess.getBalances().stream()
                                                     .map(ref -> updateAccountReference(ref, requestedAccess.getBalances())).collect(Collectors.toList());
        List<AccountReference> updatedTransactions = existingAccess.getTransactions().stream()
                                                         .map(ref -> updateAccountReference(ref, requestedAccess.getTransactions())).collect(Collectors.toList());
        AdditionalInformationAccess updatedAdditionalInformation = updateAccountReferencesInAdditionalInformation(existingAccess.getAdditionalInformationAccess(),
                                                                                                                  requestedAccess.getAdditionalInformationAccess());

        return new AccountAccess(updatedAccounts, updatedBalances, updatedTransactions, existingAccess.getAvailableAccounts(), existingAccess.getAllPsd2(),
                                 existingAccess.getAvailableAccountsWithBalance(), updatedAdditionalInformation);
    }

    private boolean hasNoAccountReferences(AccountAccess accountAccess) {
        AdditionalInformationAccess additionalInformationAccess = accountAccess.getAdditionalInformationAccess();
        boolean hasNoAdditionalInformationReferences = additionalInformationAccess == null
                                                           || CollectionUtils.isEmpty(additionalInformationAccess.getOwnerName());

        return CollectionUtils.isEmpty(accountAccess.getAccounts())
                   && CollectionUtils.isEmpty(accountAccess.getBalances())
                   && CollectionUtils.isEmpty(accountAccess.getTransactions())
                   && hasNoAdditionalInformationReferences;
    }

    private AdditionalInformationAccess updateAccountReferencesInAdditionalInformation(AdditionalInformationAccess existingAccess, AdditionalInformationAccess requestedAccess) {
        if (isAdditionalInformationAbsent(existingAccess) || isAdditionalInformationAbsent(requestedAccess)) {
            return existingAccess;
        }

        assert existingAccess.getOwnerName() != null;
        assert requestedAccess.getOwnerName() != null;
        List<AccountReference> updatedOwnerName = existingAccess.getOwnerName().stream()
                                                      .map(ref -> updateAccountReference(ref, requestedAccess.getOwnerName()))
                                                      .collect(Collectors.toList());

        return new AdditionalInformationAccess(updatedOwnerName);
    }

    private boolean isAdditionalInformationAbsent(AdditionalInformationAccess additionalInformationAccess) {
        return additionalInformationAccess == null || additionalInformationAccess.getOwnerName() == null;
    }

    private AccountReference updateAccountReference(AccountReference existingReference, List<AccountReference> requestedAspspReferences) {
        return requestedAspspReferences.stream()
                   .filter(aspsp -> aspsp.getUsedAccountReferenceSelector().equals(existingReference.getUsedAccountReferenceSelector()))
                   .filter(aspsp -> aspsp.getCurrency().equals(existingReference.getCurrency()))
                   .findFirst()
                   .orElse(existingReference);
    }
}
