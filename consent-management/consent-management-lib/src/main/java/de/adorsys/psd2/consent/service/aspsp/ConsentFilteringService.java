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

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.core.data.ais.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// ToDo: cover with tests https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1170
@Service
@RequiredArgsConstructor
public class ConsentFilteringService {
    private final ConsentDataMapper consentDataMapper;

    public List<ConsentEntity> filterAisConsentsByAspspAccountId(List<ConsentEntity> consentEntities, @Nullable String aspspAccountId) {
        if (aspspAccountId == null) {
            return consentEntities;
        }

        return consentEntities.stream()
                   .filter(consent -> containsAccountReferenceWithAccountId(consent, aspspAccountId))
                   .collect(Collectors.toList());
    }

    private boolean containsAccountReferenceWithAccountId(@NotNull ConsentEntity consentEntity, @NotNull String aspspAccountId) {
        AisConsentData aisConsentData = consentDataMapper.mapToAisConsentData(consentEntity.getData());
        AccountAccess aspspAccountAccess = aisConsentData.getAspspAccountAccess();
        Set<AccountReference> accountReferences = new HashSet<>(CollectionUtils.emptyIfNull(aspspAccountAccess.getAccounts()));
        accountReferences.addAll(CollectionUtils.emptyIfNull(aspspAccountAccess.getBalances()));
        accountReferences.addAll(CollectionUtils.emptyIfNull(aspspAccountAccess.getTransactions()));
        AdditionalInformationAccess additionalInformationAccess = aspspAccountAccess.getAdditionalInformationAccess();
        if (additionalInformationAccess != null
                && CollectionUtils.isNotEmpty(additionalInformationAccess.getOwnerName())) {
            accountReferences.addAll(additionalInformationAccess.getOwnerName());
        }

        return accountReferences.stream()
                   .anyMatch(reference -> aspspAccountId.equals(reference.getAspspAccountId()));
    }
}
