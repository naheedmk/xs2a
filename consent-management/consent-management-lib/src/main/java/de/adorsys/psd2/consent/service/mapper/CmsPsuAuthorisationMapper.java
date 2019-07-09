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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisAuthorization;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CmsPsuAuthorisationMapper {

    public CmsPsuAuthorisation mapToCmsPsuAuthorisationPis(PisAuthorization pisAuthorization) {
        CmsPsuAuthorisation result = new CmsPsuAuthorisation();

        result.setPsuId(pisAuthorization.getPsuData().getPsuId());
        result.setAuthorisationId(pisAuthorization.getExternalId());
        result.setScaStatus(pisAuthorization.getScaStatus());
        result.setAuthorisationType(pisAuthorization.getAuthorizationType());
        result.setAuthorisationExpirationTimestamp(pisAuthorization.getAuthorisationExpirationTimestamp());
        result.setScaApproach(pisAuthorization.getScaApproach());
        result.setTppOkRedirectUri(pisAuthorization.getPaymentData().getTppInfo().getRedirectUri());
        result.setTppNokRedirectUri(pisAuthorization.getPaymentData().getTppInfo().getNokRedirectUri());
        result.setTppOkRedirectUriCancellation(pisAuthorization.getPaymentData().getTppInfo().getCancelRedirectUri());
        result.setTppNokRedirectUriCancellation(pisAuthorization.getPaymentData().getTppInfo().getCancelNokRedirectUri());

        return result;
    }

    public CmsPsuAuthorisation mapToCmsPsuAuthorisationAis(AisConsentAuthorization consentAuthorization) {
        CmsPsuAuthorisation result = new CmsPsuAuthorisation();

        result.setPsuId(consentAuthorization.getPsuData().getPsuId());
        result.setAuthorisationId(consentAuthorization.getExternalId());
        result.setScaStatus(consentAuthorization.getScaStatus());
        result.setAuthorisationType(CmsAuthorisationType.CREATED);
        result.setAuthorisationExpirationTimestamp(consentAuthorization.getAuthorisationExpirationTimestamp());
        result.setScaApproach(consentAuthorization.getScaApproach());
        result.setTppOkRedirectUri(consentAuthorization.getConsent().getTppInfo().getRedirectUri());
        result.setTppNokRedirectUri(consentAuthorization.getConsent().getTppInfo().getNokRedirectUri());

        return result;
    }
}
