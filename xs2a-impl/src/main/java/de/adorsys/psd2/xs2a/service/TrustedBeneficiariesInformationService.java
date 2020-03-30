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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrustedBeneficiariesInformationService {
    private final AspspProfileServiceWrapper aspspProfileService;

    public CreateConsentReq checkIfAdditionalInformationSupported(CreateConsentReq request) {
        if (!aspspProfileService.isTrustedBeneficiariesSupported()) {
            return clearTrustedBeneficiaries(request);
        }
        return request;
    }

    private CreateConsentReq clearTrustedBeneficiaries(CreateConsentReq request) {
        AccountAccess access = request.getAccess();

        if (isConsentWithTrustedBeneficiaries(access)) {
            AdditionalInformationAccess additionalInformationAccess = access.getAdditionalInformationAccess();
            AdditionalInformationAccess additionalInformationWithoutTrustedBeneficiaries = new AdditionalInformationAccess(additionalInformationAccess.getOwnerName(), null);
            AccountAccess accessWithoutTrustedBeneficiaries = new AccountAccess(access.getAccounts(), access.getBalances(), access.getTransactions(), additionalInformationWithoutTrustedBeneficiaries);
            request.setAccess(accessWithoutTrustedBeneficiaries);
        }

        return request;
    }

    private boolean isConsentWithTrustedBeneficiaries(AccountAccess access) {
        return Optional.ofNullable(access.getAdditionalInformationAccess())
                   .map(AdditionalInformationAccess::getTrustedBeneficiaries)
                   .isPresent();
    }
}
