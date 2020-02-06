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

package de.adorsys.psd2.core.data.ais;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.psd2.core.data.Consent;
import de.adorsys.psd2.xs2a.core.authorisation.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationTemplate;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Stream;

public class AisConsent extends Consent<AisConsentData> {

    public AisConsent() {}

    public AisConsent(AisConsentData consentData, String id, String internalRequestId, ConsentStatus consentStatus, Integer frequencyPerDay, boolean recurringIndicator, boolean multilevelScaRequired,
                      LocalDate validUntil, LocalDate expireDate, LocalDate lastActionDate, OffsetDateTime creationTimestamp, OffsetDateTime statusChangeTimestamp, ConsentTppInformation consentTppInformation,
                      AuthorisationTemplate authorisationTemplate, List<PsuIdData> psuIdDataList, List<AccountConsentAuthorization> authorisations, Map<String, Integer> usages) {

        super(consentData, id, internalRequestId, consentStatus, frequencyPerDay, recurringIndicator, multilevelScaRequired,
              validUntil, expireDate, lastActionDate, creationTimestamp, statusChangeTimestamp, consentTppInformation,
              authorisationTemplate, psuIdDataList, authorisations, usages);
    }

    @Override
    public ConsentType getConsentType() {
        return ConsentType.AIS;
    }

    @JsonIgnore
    public AccountAccess getAccess() {
        return getConsentData().getUsedAccess();
    }

    public AccountAccess getAspspAccess() {
        return getConsentData().getAspspAccountAccess();
    }

    public boolean isWithBalance() {
        return getConsentData().isWithBalance();
    }

    @JsonIgnore
    public boolean isOneAccessType() {
        return !isRecurringIndicator();
    }

    @JsonIgnore
    public boolean isGlobalConsent() {
        return getConsentData().getConsentRequestType() == AisConsentRequestType.GLOBAL;
    }

    @JsonIgnore
    public boolean isConsentForAllAvailableAccounts() {
        return getConsentData().getConsentRequestType() == AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS;
    }

    @JsonIgnore
    public boolean isConsentForDedicatedAccounts() {
        return getConsentData().getConsentRequestType() == AisConsentRequestType.DEDICATED_ACCOUNTS;
    }

    public Optional<AccountConsentAuthorization> findAuthorisationInConsent(String authorisationId) {
        return getAuthorisations().stream()
                   .filter(auth -> auth.getId().equals(authorisationId))
                   .findFirst();
    }

    public boolean isConsentWithNotIbanAccount() {
        AccountAccess access = getAccess();
        if (access == null) {
            return false;
        }

        return Stream.of(access.getAccounts(), access.getBalances(), access.getTransactions())
                   .filter(Objects::nonNull)
                   .flatMap(Collection::stream)
                   .allMatch(acc -> StringUtils.isAllBlank(acc.getIban(), acc.getBban(), acc.getMsisdn()));
    }

    public boolean isConsentWithNotCardAccount() {
        AccountAccess access = getAccess();
        if (access == null) {
            return false;

        }

        return Stream.of(access.getAccounts(), access.getBalances(), access.getTransactions())
                   .filter(Objects::nonNull)
                   .flatMap(Collection::stream)
                   .allMatch(acc -> StringUtils.isAllBlank(acc.getMaskedPan(), acc.getPan()));
    }

    @JsonIgnore
    public boolean isExpired() {
        return getConsentStatus() == ConsentStatus.EXPIRED;
    }

    public Map<String, Integer> getUsageCounterMap() {
        return getUsages();
    }

    public AisConsentRequestType getAisConsentRequestType() {
        return getConsentData().getConsentRequestType();
    }
}
