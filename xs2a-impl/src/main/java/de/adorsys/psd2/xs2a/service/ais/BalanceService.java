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

package de.adorsys.psd2.xs2a.service.ais;

import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aBalancesReport;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aBalanceReportMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetBalancesReportValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.CommonAccountBalanceRequestObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_404;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;

@Slf4j
@Service
@Validated
@AllArgsConstructor
public class BalanceService {
    private final AccountSpi accountSpi;

    private final SpiToXs2aBalanceReportMapper balanceReportMapper;

    private final Xs2aAisConsentService aisConsentService;
    private final Xs2aAisConsentMapper consentMapper;
    private final TppService tppService;
    private final Xs2aEventService xs2aEventService;
    private final SpiErrorMapper spiErrorMapper;

    private final GetBalancesReportValidator getBalancesReportValidator;
    private final RequestProviderService requestProviderService;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final AccountHelperService accountHelperService;

    /**
     * Gets Balances Report based on consentId and accountId
     *
     * @param consentId  String representing an AccountConsent identification
     * @param accountId  String representing a PSU`s Account at ASPSP
     * @param requestUri the URI of incoming request
     * @return Balances Report based on consentId and accountId
     */
    public ResponseObject<Xs2aBalancesReport> getBalancesReport(String consentId, String accountId, String requestUri) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.READ_BALANCE_REQUEST_RECEIVED);

        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);

        UUID internalRequestId = requestProviderService.getInternalRequestId();
        UUID xRequestId = requestProviderService.getRequestId();

        if (!accountConsentOptional.isPresent()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID [{}]. Get balances report failed. Account consent not found by id",
                     internalRequestId, xRequestId, accountId, consentId);
            return ResponseObject.<Xs2aBalancesReport>builder()
                       .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AccountConsent accountConsent = accountConsentOptional.get();

        ValidationResult validationResult = getBalancesReportValidator.validate(
            new CommonAccountBalanceRequestObject(accountConsent, accountId, requestUri));

        if (validationResult.isNotValid()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID [{}], RequestUri [{}]. Get balances report - validation failed: {}",
                     internalRequestId, xRequestId, accountId, consentId, requestUri, validationResult.getMessageError());
            return ResponseObject.<Xs2aBalancesReport>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        Xs2aAccountAccess access = accountConsent.getAccess();
        SpiAccountReference requestedAccountReference = accountHelperService.findAccountReference(access.getAllPsd2(), access.getBalances(), accountId);
        SpiContextData contextData = accountHelperService.getSpiContextData();

        SpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId);

        SpiResponse<List<SpiAccountBalance>> spiResponse =
            accountSpi.requestBalancesForAccount(contextData, requestedAccountReference,
                                                 consentMapper.mapToSpiAccountConsent(accountConsent),
                                                 aspspConsentDataProvider);
        if (spiResponse.hasError()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID: [{}]. Get balances report failed: couldn't get balances by account id.",
                     internalRequestId, xRequestId, accountId, consentId);
            return ResponseObject.<Xs2aBalancesReport>builder()
                       .fail(new MessageError(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS)))
                       .build();
        }

        if (spiResponse.getPayload() == null) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID: [{}]. Get balances report failed: balances empty for account.",
                     internalRequestId, xRequestId, accountId, consentId);
            return ResponseObject.<Xs2aBalancesReport>builder()
                       .fail(ErrorType.AIS_404, of(RESOURCE_UNKNOWN_404))
                       .build();
        }

        Xs2aBalancesReport balancesReport = balanceReportMapper.mapToXs2aBalancesReport(requestedAccountReference,
                                                                                        spiResponse.getPayload());

        ResponseObject<Xs2aBalancesReport> response =
            ResponseObject.<Xs2aBalancesReport>builder().body(balancesReport).build();

        aisConsentService.consentActionLog(tppService.getTppId(), consentId,
                                           accountHelperService.createActionStatus(false, TypeAccess.BALANCE, response),
                                           requestUri, accountHelperService.needsToUpdateUsage(accountConsent));

        return response;
    }
}
