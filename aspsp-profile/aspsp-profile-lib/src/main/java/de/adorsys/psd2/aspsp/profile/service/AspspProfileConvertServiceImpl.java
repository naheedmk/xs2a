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

package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.config.BankProfileSetting;
import de.adorsys.psd2.aspsp.profile.config.ProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.domain.OldBankProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.OldProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.domain.ais.*;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisRedirectLinkBankSetting;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AspspProfileConvertServiceImpl implements AspspProfileConvertService {

    @Override
    public ProfileConfiguration convertProfile(OldProfileConfiguration profile) {
        OldBankProfileSetting setting = profile.getSetting();
        ConsentTypeBankSetting consentTypes = new ConsentTypeBankSetting(setting.isBankOfferedConsentSupport(),
                                                                         setting.isAllPsd2Support(),
                                                                         setting.isAvailableAccountsConsentSupported(),
                                                                         setting.getFrequencyPerDay(),
                                                                         setting.getNotConfirmedConsentExpirationPeriodMs(),
                                                                         setting.getConsentLifetime());
        AisRedirectLinkBankSetting aisRedirectLinkToOnlineBanking = new AisRedirectLinkBankSetting(setting.getAisRedirectUrlToAspsp());
        AisTransactionBankSetting transactionParameters = new AisTransactionBankSetting(setting.getAvailableBookingStatuses(),
                                                                                        setting.isTransactionsWithoutBalancesSupported(),
                                                                                        setting.getSupportedTransactionApplicationTypes().get(0));
        DeltaReportBankSetting deltaReportSettings = new DeltaReportBankSetting(setting.isEntryReferenceFromSupported(),
                                                                                setting.isDeltaListSupported());
        OneTimeConsentScaBankSetting scaRequirementsForOneTimeConsents = new OneTimeConsentScaBankSetting(setting.isScaByOneTimeAvailableAccountsConsentRequired());
        AisAspspProfileBankSetting ais = new AisAspspProfileBankSetting(consentTypes, aisRedirectLinkToOnlineBanking, transactionParameters, deltaReportSettings, scaRequirementsForOneTimeConsents);
        PisRedirectLinkBankSetting pisRedirectLinkToOnlineBanking = new PisRedirectLinkBankSetting(setting.getPisRedirectUrlToAspsp(),
                                                                                                   setting.getPisPaymentCancellationRedirectUrlToAspsp(),
                                                                                                   setting.getPaymentCancellationRedirectUrlExpirationTimeMs());
        PisAspspProfileBankSetting pis = new PisAspspProfileBankSetting(setting.getSupportedPaymentTypeAndProductMatrix()
                                                                            .entrySet().stream()
                                                                            .collect(Collectors.toMap(e -> PaymentType.valueOf(e.getKey()),
                                                                                                      Map.Entry::getValue)),
                                                                        setting.getTransactionLifetime(),
                                                                        setting.getNotConfirmedPaymentExpirationPeriodMs(),
                                                                        setting.isPaymentCancellationAuthorizationMandated(),
                                                                        pisRedirectLinkToOnlineBanking);
        PiisAspspProfileBankSetting piis = new PiisAspspProfileBankSetting(setting.isPiisConsentSupported());
        CommonAspspProfileBankSetting common = new CommonAspspProfileBankSetting(setting.getScaApproaches(),
                                                                                 setting.getScaRedirectFlow(),
                                                                                 setting.getStartAuthorisationMode(),
                                                                                 setting.isTppSignatureRequired(),
                                                                                 setting.isPsuInInitialRequestMandated(),
                                                                                 setting.getRedirectUrlExpirationTimeMs(),
                                                                                 setting.getAuthorisationExpirationTimeMs(),
                                                                                 setting.isForceXs2aBaseUrl(),
                                                                                 setting.getXs2aBaseUrl(),
                                                                                 setting.getSupportedAccountReferenceFields(),
                                                                                 setting.getMulticurrencyAccountLevel(),
                                                                                 setting.isCombinedServiceIndicator(),
                                                                                 setting.isSigningBasketSupported());
        ProfileConfiguration result = new ProfileConfiguration();
        result.setSetting(new BankProfileSetting(ais, pis, piis, common));
        return result;
    }
}
