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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.service.CorePaymentsConvertService;
import de.adorsys.psd2.consent.service.PisCommonPaymentConfirmationExpirationService;
import de.adorsys.psd2.consent.service.mapper.AuthorisationMapper;
import de.adorsys.psd2.consent.service.mapper.PisCommonPaymentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PisCancellationAuthService extends CmsAuthorisationService<PisCommonPaymentData> {
    private PisPaymentDataRepository pisPaymentDataRepository;
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    private PisCommonPaymentMapper pisCommonPaymentMapper;
    private CorePaymentsConvertService corePaymentsConvertService;

    @Autowired
    public PisCancellationAuthService(CmsPsuService cmsPsuService, PsuDataMapper psuDataMapper, AspspProfileService aspspProfileService,
                                      AuthorisationRepository authorisationRepository,
                                      PisCommonPaymentConfirmationExpirationService pisCommonPaymentConfirmationExpirationService,
                                      PisPaymentDataRepository pisPaymentDataRepository,
                                      AuthorisationMapper authorisationMapper, PisCommonPaymentDataRepository pisCommonPaymentDataRepository,
                                      PisCommonPaymentMapper pisCommonPaymentMapper, CorePaymentsConvertService corePaymentsConvertService) {
        super(cmsPsuService, psuDataMapper, aspspProfileService, authorisationMapper, authorisationRepository, pisCommonPaymentConfirmationExpirationService);
        this.pisPaymentDataRepository = pisPaymentDataRepository;
        this.pisCommonPaymentDataRepository = pisCommonPaymentDataRepository;
        this.pisCommonPaymentMapper = pisCommonPaymentMapper;
        this.corePaymentsConvertService = corePaymentsConvertService;
    }

    @Override
    public Optional<Authorisable> getNotFinalisedAuthorisationParent(String parentId) {
        // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1143
        Optional<PisCommonPaymentData> commonPaymentData = pisPaymentDataRepository.findByPaymentId(parentId)
                                                               .filter(CollectionUtils::isNotEmpty)
                                                               .map(list -> list.get(0).getPaymentData());
        if (commonPaymentData.isEmpty()) {
            commonPaymentData = pisCommonPaymentDataRepository.findByPaymentId(parentId);
        }

        return commonPaymentData
                   .filter(p -> p.getTransactionStatus().isNotFinalisedStatus())
                   .map(this::convertToCommonPayment);
    }

    @Override
    public Optional<Authorisable> getAuthorisationParent(String parentId) {
        return pisCommonPaymentDataRepository.findByPaymentId(parentId)
                   .map(con -> con);
    }

    @Override
    AuthorisationType getAuthorisationType() {
        return AuthorisationType.PIS_CANCELLATION;
    }

    @Override
    PisCommonPaymentData castToParent(Authorisable authorisable) {
        return (PisCommonPaymentData) authorisable;
    }

    private PisCommonPaymentData convertToCommonPayment(PisCommonPaymentData pisCommonPaymentData) {
        if (pisCommonPaymentData == null || pisCommonPaymentData.getPayment() != null) {
            return pisCommonPaymentData;
        }

        List<PisPayment> pisPayments = pisCommonPaymentData.getPayments().stream()
                                           .map(pisCommonPaymentMapper::mapToPisPayment)
                                           .collect(Collectors.toList());
        byte[] paymentData = corePaymentsConvertService.buildPaymentData(pisPayments, pisCommonPaymentData.getPaymentType());
        if (paymentData != null) {
            pisCommonPaymentData.setPayment(paymentData);
            return pisCommonPaymentDataRepository.save(pisCommonPaymentData);
        }

        return pisCommonPaymentData;
    }

}
