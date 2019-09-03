/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.config.BankProfileSetting;
import de.adorsys.psd2.aspsp.profile.config.ProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.StartAuthorisationMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AspspProfileServiceImpl implements AspspProfileService {
    private final ProfileConfiguration profileConfiguration;

    @Override
    public AspspSettings getAspspSettings() {
        BankProfileSetting setting = profileConfiguration.getSetting();
        return new AspspSettings(
            setting.getAis().getConsentTypes().getAccountAccessFrequencyPerDay(),
            setting.getCommon().isAisPisSessionsSupported(),
            setting.getAis().getRedirectLinkToOnlineBanking().getAisRedirectUrlToAspsp(),
            setting.getCommon().getAuthorisationExpirationTimeMs(),
            setting.getAis().getConsentTypes().isAvailableAccountsConsentSupported(),
            setting.getAis().getTransactionParameters().getAvailableBookingStatuses(),
            setting.getAis().getConsentTypes().isBankOfferedConsentSupported(),
            setting.getAis().getDeltaReportSettings().isDeltaListSupported(),
            setting.getAis().getDeltaReportSettings().isEntryReferenceFromSupported(),
            setting.getCommon().isForceXs2aBaseLinksUrl(),
            setting.getAis().getConsentTypes().isGlobalConsentSupported(),
            setting.getAis().getConsentTypes().getMaxConsentValidityDays(),
            setting.getPis().getMaxTransactionValidityDays(),
            setting.getCommon().getMulticurrencyAccountLevelSupported(),
            setting.getAis().getConsentTypes().getNotConfirmedConsentExpirationTimeMs(),
            setting.getPis().getNotConfirmedPaymentExpirationTimeMs(),
            setting.getPis().isPaymentCancellationAuthorisationMandated(),
            setting.getPis().getRedirectLinkToOnlineBanking().getPaymentCancellationRedirectUrlExpirationTimeMs(),
            setting.getPiis().isPiisConsentSupported(),
            setting.getPis().getRedirectLinkToOnlineBanking().getPisPaymentCancellationRedirectUrlToAspsp(),
            setting.getPis().getRedirectLinkToOnlineBanking().getPisRedirectUrlToAspsp(),
            setting.getCommon().isPsuInInitialRequestMandated(),
            setting.getCommon().getRedirectUrlExpirationTimeMs(),
            setting.getAis().getScaRequirementsForOneTimeConsents().isScaByOneTimeAvailableAccountsConsentRequired(),
            setting.getCommon().getScaRedirectFlow(),
            setting.getCommon().isSigningBasketSupported(),
            StartAuthorisationMode.getByValue(setting.getCommon().getStartAuthorisationMode()),
            setting.getCommon().getSupportedAccountReferenceFields(),
            setting.getPis().getSupportedPaymentTypeAndProductMatrix(),
            setting.getAis().getTransactionParameters().getSupportedTransactionApplicationTypes(),
            setting.getCommon().isTppSignatureRequired(),
            setting.getAis().getTransactionParameters().isTransactionsWithoutBalancesSupported(),
            setting.getCommon().getXs2aBaseLinksUrl()
        );
    }

    @Override
    public List<ScaApproach> getScaApproaches() {
        return profileConfiguration.getSetting()
                   .getCommon()
                   .getScaApproachesSupported();
    }
}
