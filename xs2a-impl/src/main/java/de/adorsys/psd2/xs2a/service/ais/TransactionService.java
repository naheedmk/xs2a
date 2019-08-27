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

import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsDownloadResponse;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReportByPeriodRequest;
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
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ValueValidatorService;
import de.adorsys.psd2.xs2a.service.validator.ais.account.DownloadTransactionsReportValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetTransactionDetailsValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetTransactionsReportValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.CommonAccountTransactionsRequestObject;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.DownloadTransactionListRequestObject;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.TransactionsReportByPeriodObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransactionReport;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransactionsDownloadResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;

@Slf4j
@Service
@Validated
@AllArgsConstructor
public class TransactionService {

    private final AccountSpi accountSpi;

    private final SpiToXs2aBalanceMapper balanceMapper;
    private final SpiToXs2aAccountReferenceMapper referenceMapper;
    private final SpiTransactionListToXs2aAccountReportMapper transactionsToAccountReportMapper;
    private final SpiToXs2aTransactionMapper spiToXs2aTransactionMapper;
    private final SpiToXs2aDownloadTransactionsMapper spiToXs2aDownloadTransactionsMapper;

    private final ValueValidatorService validatorService;
    private final Xs2aAisConsentService aisConsentService;
    private final Xs2aAisConsentMapper consentMapper;
    private final TppService tppService;
    private final AspspProfileServiceWrapper aspspProfileService;
    private final Xs2aEventService xs2aEventService;
    private final SpiErrorMapper spiErrorMapper;

    private final GetTransactionsReportValidator getTransactionsReportValidator;
    private final DownloadTransactionsReportValidator downloadTransactionsReportValidator;
    private final GetTransactionDetailsValidator getTransactionDetailsValidator;
    private final RequestProviderService requestProviderService;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final AccountHelperService accountHelperService;

    /**
     * Read Transaction reports of a given account addressed by "account-id", depending on the steering parameter
     * "bookingStatus" together with balances.  For a given account, additional parameters are e.g. the attributes
     * "dateFrom" and "dateTo".  The ASPSP might add balance information, if transaction lists without balances are
     * not supported.
     *
     * @param request Xs2aTransactionsReportByPeriodRequest object which contains information for building Xs2aTransactionsReport
     * @return TransactionsReport filled with appropriate transaction arrays Booked and Pending. For v1.1 balances
     * sections is added
     */
    public ResponseObject<Xs2aTransactionsReport> getTransactionsReportByPeriod(Xs2aTransactionsReportByPeriodRequest request) {
        String consentId = request.getConsentId();
        String accountId = request.getAccountId();
        xs2aEventService.recordAisTppRequest(consentId, EventType.READ_TRANSACTION_LIST_REQUEST_RECEIVED);

        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);

        UUID xRequestId = requestProviderService.getRequestId();
        UUID internalRequestId = requestProviderService.getInternalRequestId();

        if (!accountConsentOptional.isPresent()) {
            return getXs2aTransactionsReportResponseObjectFail400(consentId, accountId, xRequestId, internalRequestId);
        }

        String requestUri = request.getRequestUri();
        boolean withBalance = request.isWithBalance();
        AccountConsent accountConsent = accountConsentOptional.get();
        TransactionsReportByPeriodObject validatorObject = new TransactionsReportByPeriodObject(accountConsent, accountId,
                                                                                                withBalance, requestUri,
                                                                                                request.getEntryReferenceFrom(),
                                                                                                request.getDeltaList(),
                                                                                                request.getAcceptHeader(),
                                                                                                request.getBookingStatus());
        ValidationResult validationResult = getTransactionsReportValidator.validate(validatorObject);

        if (validationResult.isNotValid()) {
            return getXs2aTransactionsReportResponseObjectWithNotValidResult(consentId, accountId, xRequestId, internalRequestId, requestUri, withBalance, validationResult);
        }

        Xs2aAccountAccess access = accountConsent.getAccess();
        SpiAccountReference requestedAccountReference = accountHelperService.findAccountReference(access.getAllPsd2(), access.getTransactions(), accountId);

        LocalDate dateFrom = request.getDateFrom();
        LocalDate dateTo = request.getDateTo();
        LocalDate dateToChecked = Optional.ofNullable(dateTo)
                                      .orElseGet(LocalDate::now);
        validatorService.validateAccountIdPeriod(accountId, dateFrom, dateToChecked);

        boolean isTransactionsShouldContainBalances =
            !aspspProfileService.isTransactionsWithoutBalancesSupported() || withBalance;
        SpiContextData contextData = accountHelperService.getSpiContextData();

        SpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId);

        SpiResponse<SpiTransactionReport> spiResponse = accountSpi.requestTransactionsForAccount(
            contextData,
            request.getAcceptHeader(),
            isTransactionsShouldContainBalances, dateFrom, dateToChecked,
            request.getBookingStatus(),
            requestedAccountReference,
            consentMapper.mapToSpiAccountConsent(accountConsent),
            aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            return getXs2aTransactionsReportResponseObjectWithError(consentId, accountId, xRequestId, internalRequestId, spiResponse);
        }

        SpiTransactionReport spiTransactionReport = spiResponse.getPayload();

        if (spiTransactionReport == null) {
            return getXs2aTransactionsReportResponseObjectFail404(consentId, accountId, xRequestId, internalRequestId);
        }

        Optional<Xs2aAccountReport> report =
            transactionsToAccountReportMapper.mapToXs2aAccountReport(spiTransactionReport.getTransactions(), spiTransactionReport.getTransactionsRaw());

        Xs2aTransactionsReport transactionsReport = getXs2aTransactionsReport(report.orElse(null),
                                                                              requestedAccountReference,
                                                                              spiTransactionReport);
        if (spiTransactionReport.getDownloadId() != null) {
            String encodedDownloadId = Base64.getUrlEncoder().encodeToString(spiTransactionReport.getDownloadId().getBytes());
            transactionsReport.setDownloadId(encodedDownloadId);
        }

        return getXs2aTransactionsReportResponseObjectSuccess(consentId, requestUri, withBalance, accountConsent, transactionsReport);
    }

    @NotNull
    private ResponseObject<Xs2aTransactionsReport> getXs2aTransactionsReportResponseObjectSuccess(String consentId, String requestUri, boolean withBalance, AccountConsent accountConsent, Xs2aTransactionsReport transactionsReport) {
        ResponseObject<Xs2aTransactionsReport> response =
            ResponseObject.<Xs2aTransactionsReport>builder().body(transactionsReport).build();

        aisConsentService.consentActionLog(tppService.getTppId(), consentId,
                                           accountHelperService.createActionStatus(withBalance, response),
                                           requestUri, accountHelperService.needsToUpdateUsage(accountConsent));
        return response;
    }

    private ResponseObject<Xs2aTransactionsReport> getXs2aTransactionsReportResponseObjectFail404(String consentId, String accountId, UUID xRequestId, UUID internalRequestId) {
        log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID: [{}]. Get transactions report by period failed: transactions empty for account.",
                 internalRequestId, xRequestId, accountId, consentId);
        return ResponseObject.<Xs2aTransactionsReport>builder()
                   .fail(ErrorType.AIS_404, of(RESOURCE_UNKNOWN_404))
                   .build();
    }

    private ResponseObject<Xs2aTransactionsReport> getXs2aTransactionsReportResponseObjectWithError(String consentId, String accountId, UUID xRequestId, UUID internalRequestId, SpiResponse<SpiTransactionReport> spiResponse) {
        // in this particular call we use NOT_SUPPORTED to indicate that requested Content-type is not ok for us
        if (spiResponse.getErrors().get(0).getErrorCode() == SERVICE_NOT_SUPPORTED) {
            return getXs2aTransactionsReportResponseObjectWithErrorServiceNotSupported(consentId, accountId, xRequestId, internalRequestId);
        }

        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
        return getXs2aTransactionsReportResponseObjectWithErrorAnyOther(consentId, accountId, xRequestId, internalRequestId, errorHolder);
    }

    private ResponseObject<Xs2aTransactionsReport> getXs2aTransactionsReportResponseObjectWithErrorAnyOther(String consentId, String accountId, UUID xRequestId, UUID internalRequestId, ErrorHolder errorHolder) {
        log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID: [{}]. Get transactions report by period failed: Request transactions for account fail at SPI level: {}",
                 internalRequestId, xRequestId, accountId, consentId, errorHolder);
        return ResponseObject.<Xs2aTransactionsReport>builder()
                   .fail(errorHolder)
                   .build();
    }

    private ResponseObject<Xs2aTransactionsReport> getXs2aTransactionsReportResponseObjectWithErrorServiceNotSupported(String consentId, String accountId, UUID xRequestId, UUID internalRequestId) {
        log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID: [{}]. Get transactions report by period failed: requested content-type not json or text.",
                 internalRequestId, xRequestId, accountId, consentId);
        return ResponseObject.<Xs2aTransactionsReport>builder()
                   .fail(ErrorType.AIS_406, of(REQUESTED_FORMATS_INVALID))
                   .build();
    }

    private ResponseObject<Xs2aTransactionsReport> getXs2aTransactionsReportResponseObjectWithNotValidResult(String consentId, String accountId, UUID xRequestId, UUID internalRequestId, String requestUri, boolean withBalance, ValidationResult validationResult) {
        log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID [{}], WithBalance [{}], RequestUri [{}]. Get transactions report by period - validation failed: {}",
                 internalRequestId, xRequestId, accountId, consentId, withBalance, requestUri, validationResult.getMessageError());
        return ResponseObject.<Xs2aTransactionsReport>builder()
                   .fail(validationResult.getMessageError())
                   .build();
    }

    private ResponseObject<Xs2aTransactionsReport> getXs2aTransactionsReportResponseObjectFail400(String consentId, String accountId, UUID xRequestId, UUID internalRequestId) {
        log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID [{}]. Get transactions report by period failed. Account consent not found by id",
                 internalRequestId, xRequestId, accountId, consentId);
        return ResponseObject.<Xs2aTransactionsReport>builder()
                   .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                   .build();
    }

    private Xs2aTransactionsReport getXs2aTransactionsReport(Xs2aAccountReport report,
                                                             SpiAccountReference requestedAccountReference,
                                                             SpiTransactionReport spiTransactionReport) {
        Xs2aTransactionsReport transactionsReport = new Xs2aTransactionsReport();
        transactionsReport.setAccountReport(report);
        transactionsReport.setAccountReference(referenceMapper.mapToXs2aAccountReference(requestedAccountReference));
        transactionsReport.setBalances(balanceMapper.mapToXs2aBalanceList(spiTransactionReport.getBalances()));
        transactionsReport.setResponseContentType(spiTransactionReport.getResponseContentType());
        return transactionsReport;
    }

    /**
     * Gets transaction details by transaction ID
     *
     * @param consentId     String representing an AccountConsent identification
     * @param accountId     String representing a PSU`s Account at ASPSP
     * @param transactionId String representing the ASPSP identification of transaction
     * @param requestUri    the URI of incoming request
     * @return Transactions based on transaction ID.
     */
    public ResponseObject<Transactions> getTransactionDetails(String consentId, String accountId,
                                                              String transactionId, String requestUri) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.READ_TRANSACTION_DETAILS_REQUEST_RECEIVED);

        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);

        UUID internalRequestId = requestProviderService.getInternalRequestId();
        UUID xRequestId = requestProviderService.getRequestId();

        if (!accountConsentOptional.isPresent()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID [{}]. Get transaction details failed. Account consent not found by ID",
                     internalRequestId, xRequestId, accountId, consentId);
            return ResponseObject.<Transactions>builder()
                       .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AccountConsent accountConsent = accountConsentOptional.get();

        ValidationResult validationResult = getTransactionDetailsValidator.validate(
            new CommonAccountTransactionsRequestObject(accountConsent, accountId, requestUri));

        if (validationResult.isNotValid()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID [{}], RequestUri [{}]. Get transaction details - validation failed: {}",
                     internalRequestId, xRequestId, accountId, consentId, requestUri, validationResult.getMessageError());
            return ResponseObject.<Transactions>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        Xs2aAccountAccess access = accountConsent.getAccess();
        SpiAccountReference requestedAccountReference = accountHelperService.findAccountReference(access.getAllPsd2(), access.getTransactions(), accountId);
        validatorService.validateAccountIdTransactionId(accountId, transactionId);

        SpiContextData contextData = accountHelperService.getSpiContextData();

        SpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId);

        SpiResponse<SpiTransaction> spiResponse =
            accountSpi.requestTransactionForAccountByTransactionId(contextData, transactionId, requestedAccountReference, consentMapper.mapToSpiAccountConsent(accountConsent), aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
            log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID: [{}]. Get transaction details failed: Request transactions for account fail at SPI level: {}",
                     internalRequestId, xRequestId, accountId, consentId, errorHolder);
            return ResponseObject.<Transactions>builder()
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        SpiTransaction payload = spiResponse.getPayload();

        if (payload == null) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID: [{}]. Get transaction details failed: transaction details empty for account and transaction.",
                     internalRequestId, xRequestId, accountId, consentId);
            return ResponseObject.<Transactions>builder()
                       .fail(ErrorType.AIS_404, of(RESOURCE_UNKNOWN_404))
                       .build();
        }

        Transactions transactions = spiToXs2aTransactionMapper.mapToXs2aTransaction(payload);

        ResponseObject<Transactions> response =
            ResponseObject.<Transactions>builder()
                .body(transactions)
                .build();

        aisConsentService.consentActionLog(tppService.getTppId(), consentId,
                                           accountHelperService.createActionStatus(false, response),
                                           requestUri, accountHelperService.needsToUpdateUsage(accountConsent));

        return response;
    }

    /**
     * Gets stream with transaction list by consent ID, account ID and download ID
     *
     * @param consentId  String representing an AccountConsent identification
     * @param accountId  String representing a PSU`s Account at ASPSP
     * @param downloadId String representing the download identifier
     * @return Response with transaction list stream.
     */
    public ResponseObject<Xs2aTransactionsDownloadResponse> downloadTransactions(String consentId, String accountId, String downloadId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.DOWNLOAD_TRANSACTION_LIST_REQUEST_RECEIVED);

        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);

        UUID internalRequestId = requestProviderService.getInternalRequestId();
        UUID xRequestId = requestProviderService.getRequestId();

        if (!accountConsentOptional.isPresent()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}], Account-ID: [{}], Download-ID: [{}]. Download transactions failed. Account consent not found by ID",
                     internalRequestId, xRequestId, consentId, accountId, downloadId);
            return ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                       .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AccountConsent accountConsent = accountConsentOptional.get();
        ValidationResult validationResult = downloadTransactionsReportValidator.validate(
            new DownloadTransactionListRequestObject(accountConsent));

        if (validationResult.isNotValid()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}], Account-ID: [{}], Download-ID: [{}]. Download transactions - validation failed: {}",
                     internalRequestId, xRequestId, consentId, accountId, downloadId, validationResult.getMessageError());
            return ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        String decodedDownloadId = new String(Base64.getUrlDecoder().decode(downloadId));
        SpiContextData contextData = accountHelperService.getSpiContextData();
        SpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId);
        SpiResponse<SpiTransactionsDownloadResponse> spiResponse = accountSpi.requestTransactionsByDownloadLink(contextData,
                                                                                                                consentMapper.mapToSpiAccountConsent(accountConsent),
                                                                                                                decodedDownloadId,
                                                                                                                aspspConsentDataProvider);
        if (spiResponse.hasError()) {
            log.info("X-Request-ID: [{}], Consent-ID [{}], Account-ID: [{}], Download-ID: [{}]. Download transactions failed: couldn't get download transactions stream by link.",
                     xRequestId, consentId, accountId, downloadId);
            return ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                       .fail(new MessageError(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS)))
                       .build();
        }

        if (spiResponse.getPayload() == null) {
            log.info("X-Request-ID: [{}], Consent-ID [{}], Account-ID: [{}], Download-ID: [{}]. Download transactions failed: spiResponse is empty.",
                     xRequestId, consentId, accountId, downloadId);
            return ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                       .fail(ErrorType.AIS_404, of(RESOURCE_UNKNOWN_404))
                       .build();
        }

        SpiTransactionsDownloadResponse spiPayload = spiResponse.getPayload();
        return ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                   .body(spiToXs2aDownloadTransactionsMapper.mapToXs2aTransactionsDownloadResponse(spiPayload))
                   .build();
    }
}
