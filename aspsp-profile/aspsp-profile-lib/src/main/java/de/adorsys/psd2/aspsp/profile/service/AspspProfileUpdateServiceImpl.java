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
        setting.getAis().getConsentTypes().setAccountAccessFrequencyPerDay(aspspSettings.getAccountAccessFrequencyPerDay());
        setting.getCommon().setAisPisSessionsSupported(aspspSettings.isAisPisSessionsSupported());
        setting.getAis().getRedirectLinkToOnlineBanking().setAisRedirectUrlToAspsp(aspspSettings.getAisRedirectUrlToAspsp());
        setting.getCommon().setAuthorisationExpirationTimeMs(aspspSettings.getAuthorisationExpirationTimeMs());
        setting.getAis().getConsentTypes().setAvailableAccountsConsentSupported(aspspSettings.isAvailableAccountsConsentSupported());
        setting.getAis().getTransactionParameters().setAvailableBookingStatuses(aspspSettings.getAvailableBookingStatuses());
        setting.getAis().getConsentTypes().setBankOfferedConsentSupported(aspspSettings.isBankOfferedConsentSupported());
        setting.getAis().getDeltaReportSettings().setDeltaListSupported(aspspSettings.isDeltaListSupported());
        setting.getAis().getDeltaReportSettings().setEntryReferenceFromSupported(aspspSettings.isEntryReferenceFromSupported());
        setting.getCommon().setForceXs2aBaseLinksUrl(aspspSettings.isForceXs2aBaseLinksUrl());
        setting.getAis().getConsentTypes().setGlobalConsentSupported(aspspSettings.isGlobalConsentSupported());
        setting.getAis().getConsentTypes().setMaxConsentValidityDays(aspspSettings.getMaxConsentValidityDays());
        setting.getPis().setMaxTransactionValidityDays(aspspSettings.getMaxTransactionValidityDays());
        setting.getCommon().setMulticurrencyAccountLevelSupported(aspspSettings.getMulticurrencyAccountLevelSupported());
        setting.getAis().getConsentTypes().setNotConfirmedConsentExpirationTimeMs(aspspSettings.getNotConfirmedConsentExpirationTimeMs());
        setting.getPis().setNotConfirmedPaymentExpirationTimeMs(aspspSettings.getNotConfirmedPaymentExpirationTimeMs());
        setting.getPis().setPaymentCancellationAuthorisationMandated(aspspSettings.isPaymentCancellationAuthorisationMandated());
        setting.getPis().getRedirectLinkToOnlineBanking().setPaymentCancellationRedirectUrlExpirationTimeMs(aspspSettings.getPaymentCancellationRedirectUrlExpirationTimeMs());
        setting.getPiis().setPiisConsentSupported(aspspSettings.isPiisConsentSupported());
        setting.getPis().getRedirectLinkToOnlineBanking().setPisPaymentCancellationRedirectUrlToAspsp(aspspSettings.getPisPaymentCancellationRedirectUrlToAspsp());
        setting.getPis().getRedirectLinkToOnlineBanking().setPisRedirectUrlToAspsp(aspspSettings.getPisRedirectUrlToAspsp());
        setting.getCommon().setPsuInInitialRequestMandated(aspspSettings.isPsuInInitialRequestMandated());
        setting.getCommon().setRedirectUrlExpirationTimeMs(aspspSettings.getRedirectUrlExpirationTimeMs());
        setting.getAis().getScaRequirementsForOneTimeConsents().setScaByOneTimeAvailableAccountsConsentRequired(aspspSettings.isScaByOneTimeAvailableAccountsConsentRequired());
        setting.getCommon().setScaRedirectFlow(aspspSettings.getScaRedirectFlow());
        setting.getCommon().setSigningBasketSupported(aspspSettings.isSigningBasketSupported());
        setting.getCommon().setStartAuthorisationMode(aspspSettings.getStartAuthorisationMode() == null ? StartAuthorisationMode.AUTO.getValue() : aspspSettings.getStartAuthorisationMode().getValue());
        setting.getCommon().setSupportedAccountReferenceFields(aspspSettings.getSupportedAccountReferenceFields());
        setting.getPis().setSupportedPaymentTypeAndProductMatrix(aspspSettings.getSupportedPaymentTypeAndProductMatrix());
        setting.getAis().getTransactionParameters().setSupportedTransactionApplicationTypes(aspspSettings.getSupportedTransactionApplicationTypes());
        setting.getCommon().setTppSignatureRequired(aspspSettings.isTppSignatureRequired());
        setting.getAis().getTransactionParameters().setTransactionsWithoutBalancesSupported(aspspSettings.isTransactionsWithoutBalancesSupported());
        setting.getCommon().setXs2aBaseLinksUrl(aspspSettings.getXs2aBaseLinksUrl());
    }
}
