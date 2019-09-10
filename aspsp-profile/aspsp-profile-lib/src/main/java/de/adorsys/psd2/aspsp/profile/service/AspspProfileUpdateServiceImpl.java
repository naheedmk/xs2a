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
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AspspProfileUpdateServiceImpl implements AspspProfileUpdateService {

    private final ProfileConfiguration profileConfiguration;

    /**
     * Update sca approach
     *
     * @param scaApproaches the new value of scaApproach list
     */
    @Override
    public void updateScaApproaches(List<ScaApproach> scaApproaches) {
        profileConfiguration.getSetting()
            .getCommon()
            .setScaApproachesSupported(scaApproaches);
    }

    /**
     * Update all aspsp settings (frequency per day, combined service indicator, available payment products, available payment types,
     * is tpp signature required, PIS redirect URL, AIS redirect URL, multicurrency account level, is bank offered consent supported,
     * available booking statuses, supported account reference fields, consent lifetime, transaction lifetime, allPsd2 support,
     * transactions without balances support, signing basket support, is payment cancellation authorisation mandated, piis consent support,
     * delta report support, redirect url expiration time, type of authorisation start, etc.) except SCA approach
     *
     * @param aspspSettings new aspsp specific settings which to be stored in profile
     */
    @Override
    public void updateAspspSettings(@NotNull AspspSettings aspspSettings) {
        BankProfileSetting setting = profileConfiguration.getSetting();

        setting.getAis().getConsentTypes().setAccountAccessFrequencyPerDay(aspspSettings.getAis().getConsentTypes().getAccountAccessFrequencyPerDay());
        setting.getAis().getConsentTypes().setAvailableAccountsConsentSupported(aspspSettings.getAis().getConsentTypes().isAvailableAccountsConsentSupported());
        setting.getAis().getConsentTypes().setBankOfferedConsentSupported(aspspSettings.getAis().getConsentTypes().isBankOfferedConsentSupported());
        setting.getAis().getConsentTypes().setGlobalConsentSupported(aspspSettings.getAis().getConsentTypes().isGlobalConsentSupported());
        setting.getAis().getConsentTypes().setMaxConsentValidityDays(aspspSettings.getAis().getConsentTypes().getMaxConsentValidityDays());
        setting.getAis().getConsentTypes().setNotConfirmedConsentExpirationTimeMs(aspspSettings.getAis().getConsentTypes().getNotConfirmedConsentExpirationTimeMs());
        setting.getAis().getRedirectLinkToOnlineBanking().setAisRedirectUrlToAspsp(aspspSettings.getAis().getRedirectLinkToOnlineBanking().getAisRedirectUrlToAspsp());
        setting.getAis().getDeltaReportSettings().setDeltaListSupported(aspspSettings.getAis().getDeltaReportSettings().isDeltaListSupported());
        setting.getAis().getDeltaReportSettings().setEntryReferenceFromSupported(aspspSettings.getAis().getDeltaReportSettings().isEntryReferenceFromSupported());
        setting.getAis().getTransactionParameters().setAvailableBookingStatuses(aspspSettings.getAis().getTransactionParameters().getAvailableBookingStatuses());
        setting.getAis().getTransactionParameters().setSupportedTransactionApplicationType(aspspSettings.getAis().getTransactionParameters().getSupportedTransactionApplicationType());
        setting.getAis().getTransactionParameters().setTransactionsWithoutBalancesSupported(aspspSettings.getAis().getTransactionParameters().isTransactionsWithoutBalancesSupported());
        setting.getAis().getScaRequirementsForOneTimeConsents().setScaByOneTimeAvailableAccountsConsentRequired(aspspSettings.getAis().getScaRequirementsForOneTimeConsents().isScaByOneTimeAvailableAccountsConsentRequired());
        setting.getPis().setSupportedPaymentTypeAndProductMatrix(aspspSettings.getPis().getSupportedPaymentTypeAndProductMatrix());
        setting.getPis().setMaxTransactionValidityDays(aspspSettings.getPis().getMaxTransactionValidityDays());
        setting.getPis().setNotConfirmedPaymentExpirationTimeMs(aspspSettings.getPis().getNotConfirmedPaymentExpirationTimeMs());
        setting.getPis().setPaymentCancellationAuthorisationMandated(aspspSettings.getPis().isPaymentCancellationAuthorisationMandated());
        setting.getPis().getRedirectLinkToOnlineBanking().setPaymentCancellationRedirectUrlExpirationTimeMs(aspspSettings.getPis().getRedirectLinkToOnlineBanking().getPaymentCancellationRedirectUrlExpirationTimeMs());
        setting.getPis().getRedirectLinkToOnlineBanking().setPisPaymentCancellationRedirectUrlToAspsp(aspspSettings.getPis().getRedirectLinkToOnlineBanking().getPisPaymentCancellationRedirectUrlToAspsp());
        setting.getPis().getRedirectLinkToOnlineBanking().setPisRedirectUrlToAspsp(aspspSettings.getPis().getRedirectLinkToOnlineBanking().getPisRedirectUrlToAspsp());
        setting.getPiis().setPiisConsentSupported(aspspSettings.getPiis().isPiisConsentSupported());
        setting.getCommon().setAisPisSessionsSupported(aspspSettings.getCommon().isAisPisSessionsSupported());
        setting.getCommon().setTppSignatureRequired(aspspSettings.getCommon().isTppSignatureRequired());
        setting.getCommon().setForceXs2aBaseLinksUrl(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        setting.getCommon().setAuthorisationExpirationTimeMs(aspspSettings.getCommon().getAuthorisationExpirationTimeMs());
        setting.getCommon().setMulticurrencyAccountLevelSupported(aspspSettings.getCommon().getMulticurrencyAccountLevelSupported());
        setting.getCommon().setPsuInInitialRequestMandated(aspspSettings.getCommon().isPsuInInitialRequestMandated());
        setting.getCommon().setRedirectUrlExpirationTimeMs(aspspSettings.getCommon().getRedirectUrlExpirationTimeMs());
        setting.getCommon().setScaRedirectFlow(aspspSettings.getCommon().getScaRedirectFlow());
        setting.getCommon().setSigningBasketSupported(aspspSettings.getCommon().isSigningBasketSupported());
        setting.getCommon().setStartAuthorisationMode(aspspSettings.getCommon().getStartAuthorisationMode() == null ? "AUTO" : aspspSettings.getCommon().getStartAuthorisationMode().getValue());
        setting.getCommon().setSupportedAccountReferenceFields(aspspSettings.getCommon().getSupportedAccountReferenceFields());
        setting.getCommon().setXs2aBaseLinksUrl(aspspSettings.getCommon().getXs2aBaseLinksUrl());
    }
}
