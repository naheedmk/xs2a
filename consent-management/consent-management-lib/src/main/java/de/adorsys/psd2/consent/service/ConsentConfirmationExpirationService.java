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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.domain.account.Consent;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentConfirmationExpirationService {
    private final ConsentJpaRepository consentJpaRepository;
    private final AspspProfileService aspspProfileService;

    @Transactional
    public Consent checkAndUpdateOnConfirmationExpiration(Consent consent) {
        if (isConsentConfirmationExpired(consent)) {
            log.info("Consent ID: [{}]. Consent is expired", consent.getExternalId());
            return updateConsentOnConfirmationExpiration(consent);
        }
        return consent;
    }

    public boolean isConsentConfirmationExpired(Consent consent) {
        long expirationPeriodMs = aspspProfileService.getAspspSettings().getAis().getConsentTypes().getNotConfirmedConsentExpirationTimeMs();
        return consent != null && consent.isConfirmationExpired(expirationPeriodMs);
    }

    @Transactional
    public Consent expireConsent(Consent consent) {
        LocalDate now = LocalDate.now();
        consent.setConsentStatus(ConsentStatus.EXPIRED);
        consent.setExpireDate(now);
        consent.setLastActionDate(now);
        return consentJpaRepository.save(consent);
    }

    @Transactional
    public Consent updateConsentOnConfirmationExpiration(Consent consent) {
        return consentJpaRepository.save(obsoleteConsent(consent));
    }

    @Transactional
    public List<Consent> updateConsentListOnConfirmationExpiration(List<Consent> consents) {
        return IterableUtils.toList(consentJpaRepository.saveAll(obsoleteConsentList(consents)));
    }

    private List<Consent> obsoleteConsentList(List<Consent> consents) {
        return consents.stream()
                   .map(this::obsoleteConsent)
                   .collect(Collectors.toList());
    }

    private Consent obsoleteConsent(Consent consent) {
        consent.setConsentStatus(ConsentStatus.REJECTED);
        consent.getAuthorizations().forEach(auth -> auth.setScaStatus(ScaStatus.FAILED));
        consent.setLastActionDate(LocalDate.now());
        return consent;
    }
}
