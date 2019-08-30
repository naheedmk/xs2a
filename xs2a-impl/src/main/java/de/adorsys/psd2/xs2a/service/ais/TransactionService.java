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

        Optional<AccountConsent> accountConsentOptional = recordAisTppRequestAndGetAccountConsentOptional(request.getConsentId(),
                                                                                                          EventType.READ_TRANSACTION_LIST_REQUEST_RECEIVED);

        if (!accountConsentOptional.isPresent()) {
            return buildResponseXs2aTransactionsReportWithError400(request);
        }

        AccountConsent accountConsent = accountConsentOptional.get();
        ValidationResult validationResult = getValidationResultForTransactionsReportByPeriod(request, accountConsent);

        if (validationResult.isNotValid()) {
            return buildResponseObjectTransactionsReportWithValidationError(request, validationResult.getMessageError());
        }

        SpiResponse<SpiTransactionReport> spiResponse = getSpiResponseSpiTransactionReport(request, accountConsent);

        if (spiResponse.hasError()) {
            return checkSpiResponseForTransactionsReport(request, spiResponse);
        }

        return getTransactionsReportByPeriodSuccess(request, accountConsent, spiResponse.getPayload());
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
    public ResponseObject<Transactions> getTransactionDetails(String consentId,
                                                              String accountId,
                                                              String transactionId,
                                                              String requestUri) {
        Optional<AccountConsent> accountConsentOptional = recordAisTppRequestAndGetAccountConsentOptional(consentId, EventType.READ_TRANSACTION_DETAILS_REQUEST_RECEIVED);

        if (!accountConsentOptional.isPresent()) {
            return buildResponseObjectTransactionsWithError400(consentId, accountId);
        }

        AccountConsent accountConsent = accountConsentOptional.get();
        ValidationResult validationResult = getValidationResultForCommonAccountTransactions(accountId, requestUri, accountConsent);

        if (validationResult.isNotValid()) {
            return buildResponseObjectTransactionsWithValidationError(consentId, accountId, requestUri, validationResult.getMessageError());
        }

        SpiResponse<SpiTransaction> spiResponse = getSpiResponseSpiTransaction(accountConsent, consentId, accountId, transactionId);

        if (spiResponse.hasError()) {
            return checkSpiResponseForTransactions(consentId, accountId, spiResponse);
        }

        SpiTransaction payload = spiResponse.getPayload();

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
        Optional<AccountConsent> accountConsentOptional = recordAisTppRequestAndGetAccountConsentOptional(consentId, EventType.DOWNLOAD_TRANSACTION_LIST_REQUEST_RECEIVED);

        if (!accountConsentOptional.isPresent()) {
            return buildResponseObjectTransactionsDownloadResponseWithError400(consentId, accountId, downloadId);
        }

        AccountConsent accountConsent = accountConsentOptional.get();
        ValidationResult validationResult = getValidationResultForDownloadTransactionRequest(accountConsent);

        if (validationResult.isNotValid()) {
            return buildResponseObjectTransactionsDownloadResponseWithValidationError(consentId, accountId, downloadId, validationResult.getMessageError());
        }

        SpiResponse<SpiTransactionsDownloadResponse> spiResponse = getSpiResponseSpiTransactionsDownloadResponse(accountConsent, consentId, downloadId);

        if (spiResponse.hasError()) {
            return checkSpiResponseForTransactionDownloadResponse(consentId, accountId, downloadId, spiResponse);
        }

        SpiTransactionsDownloadResponse spiPayload = spiResponse.getPayload();
        Xs2aTransactionsDownloadResponse transactionsDownloadResponse = spiToXs2aDownloadTransactionsMapper.mapToXs2aTransactionsDownloadResponse(spiPayload);

        return ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                   .body(transactionsDownloadResponse)
                   .build();
    }

    // first called in getTransactionsReportByPeriod, getTransactionDetails, downloadTransactions
    private Optional<AccountConsent> recordAisTppRequestAndGetAccountConsentOptional(String consentId, EventType eventType) {
        xs2aEventService.recordAisTppRequest(consentId, eventType);
        return aisConsentService.getAccountConsentById(consentId);
    }

    // return first in getTransactionsReportByPeriod if !accountConsentOptional.isPresent()
    private ResponseObject<Xs2aTransactionsReport> buildResponseXs2aTransactionsReportWithError400(Xs2aTransactionsReportByPeriodRequest request) {
        log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID [{}]. Get transactions report by period failed. Account consent not found by id",
                 requestProviderService.getInternalRequestId(),
                 requestProviderService.getRequestId(),
                 request.getAccountId(),
                 request.getConsentId());
        return ResponseObject.<Xs2aTransactionsReport>builder()
                   .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                   .build();
    }

    // return first in getTransactionDetails if !accountConsentOptional.isPresent()
    private ResponseObject<Transactions> buildResponseObjectTransactionsWithError400(String consentId, String accountId) {
        log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID [{}]. Get transaction details failed. Account consent not found by ID",
                 requestProviderService.getInternalRequestId(),
                 requestProviderService.getRequestId(),
                 accountId,
                 consentId);
        return ResponseObject.<Transactions>builder()
                   .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                   .build();
    }

    // return first in downloadTransactions if !accountConsentOptional.isPresent()
    private ResponseObject<Xs2aTransactionsDownloadResponse> buildResponseObjectTransactionsDownloadResponseWithError400(String consentId,
                                                                                                                             String accountId,
                                                                                                                             String downloadId) {
        log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}], Account-ID: [{}], Download-ID: [{}]. Download transactions failed. Account consent not found by ID",
                 requestProviderService.getInternalRequestId(),
                 requestProviderService.getRequestId(),
                 consentId,
                 accountId,
                 downloadId);
        return ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                   .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                   .build();

    }

    // second call in getTransactionsReportByPeriod
    @NotNull
    private ValidationResult getValidationResultForTransactionsReportByPeriod(Xs2aTransactionsReportByPeriodRequest request, AccountConsent accountConsent) {
        TransactionsReportByPeriodObject validatorObject = new TransactionsReportByPeriodObject(accountConsent, request.getAccountId(),
                                                                                                request.isWithBalance(), request.getRequestUri(),
                                                                                                request.getEntryReferenceFrom(),
                                                                                                request.getDeltaList(),
                                                                                                request.getAcceptHeader(),
                                                                                                request.getBookingStatus());
        return getTransactionsReportValidator.validate(validatorObject);
    }

    // return second in getTransactionsReportByPeriod if validationResult.isNotValid()
    private ResponseObject<Xs2aTransactionsReport> buildResponseObjectTransactionsReportWithValidationError(Xs2aTransactionsReportByPeriodRequest request,
                                                                                                            MessageError messageError) {
        log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID [{}], WithBalance [{}], RequestUri [{}]. Get transactions report by period - validation failed: {}",
                 requestProviderService.getInternalRequestId(),
                 requestProviderService.getRequestId(),
                 request.getAccountId(),
                 request.getConsentId(),
                 request.isWithBalance(),
                 request.getRequestUri(),
                 messageError);
        return ResponseObject.<Xs2aTransactionsReport>builder()
                   .fail(messageError)
                   .build();
    }

    // second call in getTransactionDetails
    @NotNull
    private ValidationResult getValidationResultForCommonAccountTransactions(String accountId,
                                                                             String requestUri,
                                                                             AccountConsent accountConsent) {
        CommonAccountTransactionsRequestObject validatorObject = new CommonAccountTransactionsRequestObject(accountConsent,
                                                                                                            accountId,
                                                                                                            requestUri);
        return getTransactionDetailsValidator.validate(validatorObject);
    }

    // return second in getTransactionDetails if validationResult.isNotValid()
    private ResponseObject<Transactions> buildResponseObjectTransactionsWithValidationError(String consentId,
                                                                                            String accountId,
                                                                                            String requestUri,
                                                                                            MessageError messageError) {
        log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID [{}], RequestUri [{}]. Get transaction details - validation failed: {}",
                 requestProviderService.getInternalRequestId(),
                 requestProviderService.getRequestId(),
                 accountId,
                 consentId,
                 requestUri,
                 messageError);
        return ResponseObject.<Transactions>builder()
                   .fail(messageError)
                   .build();
    }

    // second call in downloadTransactions
    @NotNull
    private ValidationResult getValidationResultForDownloadTransactionRequest(AccountConsent accountConsent) {
        DownloadTransactionListRequestObject validatorObject = new DownloadTransactionListRequestObject(accountConsent);
        return downloadTransactionsReportValidator.validate(validatorObject);
    }

    // return second in downloadTransactions if validationResult.isNotValid()
    private ResponseObject<Xs2aTransactionsDownloadResponse> buildResponseObjectTransactionsDownloadResponseWithValidationError(String consentId,
                                                                                                                                String accountId,
                                                                                                                                String downloadId,
                                                                                                                                MessageError messageError) {
        log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}], Account-ID: [{}], Download-ID: [{}]. Download transactions - validation failed: {}",
                 requestProviderService.getInternalRequestId(),
                 requestProviderService.getRequestId(),
                 consentId,
                 accountId,
                 downloadId,
                 messageError);
        return ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                   .fail(messageError)
                   .build();
    }

    // third call in getTransactionsReportByPeriod
    @NotNull
    private SpiResponse<SpiTransactionReport> getSpiResponseSpiTransactionReport(Xs2aTransactionsReportByPeriodRequest request, AccountConsent accountConsent) {
        LocalDate dateFrom = request.getDateFrom();
        LocalDate dateToChecked = Optional.ofNullable(request.getDateTo())
                                      .orElseGet(LocalDate::now);

        validatorService.validateAccountIdPeriod(request.getAccountId(), dateFrom, dateToChecked);

        boolean isTransactionsShouldContainBalances =
            !aspspProfileService.isTransactionsWithoutBalancesSupported() || request.isWithBalance();

        return accountSpi.requestTransactionsForAccount(accountHelperService.getSpiContextData(),
                                                        request.getAcceptHeader(),
                                                        isTransactionsShouldContainBalances,
                                                        dateFrom,
                                                        dateToChecked,
                                                        request.getBookingStatus(),
                                                        getRequestedAccountReference(accountConsent, request.getAccountId()),
                                                        consentMapper.mapToSpiAccountConsent(accountConsent),
                                                        aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getConsentId()));
    }

    // return third in getTransactionsReportByPeriod if spiResponse.hasError()
    private ResponseObject<Xs2aTransactionsReport> checkSpiResponseForTransactionsReport(Xs2aTransactionsReportByPeriodRequest request,
                                                                                         SpiResponse<SpiTransactionReport> spiResponse) {
        // in this particular call we use NOT_SUPPORTED to indicate that requested Content-type is not ok for us
        if (spiResponse.getErrors().get(0).getErrorCode() == SERVICE_NOT_SUPPORTED) {
            return buildResponseObjectTransactionsReportWithSpiResponseError406(request);
        }

        if (spiResponse.getPayload() == null) {
            return buildResponseObjectTransactionsReportWithSpiResponseError404(request);
        }

        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
        return buildResponseObjectTransactionsReportWithSpiResponseOtherError(request, errorHolder);
    }

    // return first in checkSpiResponseForTransactionsReport if error code is SERVICE_NOT_SUPPORTED
    private ResponseObject<Xs2aTransactionsReport> buildResponseObjectTransactionsReportWithSpiResponseError406(Xs2aTransactionsReportByPeriodRequest request) {
        log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID: [{}]. Get transactions report by period failed: requested content-type not json or text.",
                 requestProviderService.getInternalRequestId(),
                 requestProviderService.getRequestId(),
                 request.getAccountId(),
                 request.getConsentId());
        return ResponseObject.<Xs2aTransactionsReport>builder()
                   .fail(ErrorType.AIS_406, of(REQUESTED_FORMATS_INVALID))
                   .build();
    }

    // return second in checkSpiResponseForTransactionsReport if payload is null
    private ResponseObject<Xs2aTransactionsReport> buildResponseObjectTransactionsReportWithSpiResponseError404(Xs2aTransactionsReportByPeriodRequest request) {
        log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID: [{}]. Get transactions report by period failed: transactions empty for account.",
                 requestProviderService.getInternalRequestId(),
                 requestProviderService.getRequestId(),
                 request.getAccountId(),
                 request.getConsentId());
        return ResponseObject.<Xs2aTransactionsReport>builder()
                   .fail(ErrorType.AIS_404, of(RESOURCE_UNKNOWN_404))
                   .build();
    }

    // return third in checkSpiResponseForTransactionsReport in else cases
    private ResponseObject<Xs2aTransactionsReport> buildResponseObjectTransactionsReportWithSpiResponseOtherError(Xs2aTransactionsReportByPeriodRequest request,
                                                                                                                  ErrorHolder errorHolder) {
        log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID: [{}]. Get transactions report by period failed: Request transactions for account fail at SPI level: {}",
                 requestProviderService.getInternalRequestId(),
                 requestProviderService.getRequestId(),
                 request.getAccountId(),
                 request.getConsentId(),
                 errorHolder);
        return ResponseObject.<Xs2aTransactionsReport>builder()
                   .fail(errorHolder)
                   .build();
    }

    // third call in getTransactionDetails
    @NotNull
    private SpiResponse<SpiTransaction> getSpiResponseSpiTransaction(AccountConsent accountConsent,
                                                                     String consentId,
                                                                     String accountId,
                                                                     String transactionId) {
        validatorService.validateAccountIdTransactionId(accountId, transactionId);

        return accountSpi.requestTransactionForAccountByTransactionId(accountHelperService.getSpiContextData(),
                                                                      transactionId,
                                                                      getRequestedAccountReference(accountConsent, accountId),
                                                                      consentMapper.mapToSpiAccountConsent(accountConsent),
                                                                      aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));
    }

    //return third in getTransactionDetails when spiResponse.hasError()
    private ResponseObject<Transactions> checkSpiResponseForTransactions(String consentId, String accountId, SpiResponse<SpiTransaction> spiResponse) {
        if (spiResponse.getPayload() == null) {
            return buildResponseObjectTransactionsWithSpiResponseError404(consentId, accountId);
        }

        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
        return buildResponseObjectTransactionsWithSpiResponseOtherError(consentId, accountId, errorHolder);
    }

    // return first in checkSpiResponseForTransactions if payload is null
    private ResponseObject<Transactions> buildResponseObjectTransactionsWithSpiResponseError404(String consentId, String accountId) {
        log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID: [{}]. Get transaction details failed: transaction details empty for account and transaction.",
                 requestProviderService.getInternalRequestId(),
                 requestProviderService.getRequestId(),
                 accountId,
                 consentId);
        return ResponseObject.<Transactions>builder()
                   .fail(ErrorType.AIS_404, of(RESOURCE_UNKNOWN_404))
                   .build();
    }

    // return second in checkSpiResponseForTransactions in else cases
    private ResponseObject<Transactions> buildResponseObjectTransactionsWithSpiResponseOtherError(String consentId, String accountId, ErrorHolder errorHolder) {
        log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID: [{}]. Get transaction details failed: Request transactions for account fail at SPI level: {}",
                 requestProviderService.getInternalRequestId(),
                 requestProviderService.getRequestId(),
                 accountId,
                 consentId,
                 errorHolder);
        return ResponseObject.<Transactions>builder()
                   .fail(new MessageError(errorHolder))
                   .build();
    }

    // third call in downloadTransactions
    @NotNull
    private SpiResponse<SpiTransactionsDownloadResponse> getSpiResponseSpiTransactionsDownloadResponse(AccountConsent accountConsent,
                                                                                                       String consentId,
                                                                                                       String downloadId) {
        String decodedDownloadId = new String(Base64.getUrlDecoder().decode(downloadId));
        return accountSpi.requestTransactionsByDownloadLink(accountHelperService.getSpiContextData(),
                                                            consentMapper.mapToSpiAccountConsent(accountConsent),
                                                            decodedDownloadId,
                                                            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));
    }

    //return third in downloadTransactions when spiResponse.hasError()
    private ResponseObject<Xs2aTransactionsDownloadResponse> checkSpiResponseForTransactionDownloadResponse(String consentId, String accountId, String downloadId, SpiResponse<SpiTransactionsDownloadResponse> spiResponse) {
        if (spiResponse.getPayload() == null) {
            return buildResponseObjectTransactionsDownloadResponseWithSpiResponseError404(consentId, accountId, downloadId);
        }
        return buildResponseObjectTransactionsDownloadResponseWithSpiResponseOtherError(consentId, accountId, downloadId, spiResponse);
    }

    //return first in checkSpiResponseForTransactionDownloadResponse if payload is null
    private ResponseObject<Xs2aTransactionsDownloadResponse> buildResponseObjectTransactionsDownloadResponseWithSpiResponseError404(String consentId, String accountId, String downloadId) {
        log.info("X-Request-ID: [{}], Consent-ID [{}], Account-ID: [{}], Download-ID: [{}]. Download transactions failed: spiResponse is empty.",
                 requestProviderService.getRequestId(), consentId, accountId, downloadId);
        return ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                   .fail(ErrorType.AIS_404, of(RESOURCE_UNKNOWN_404))
                   .build();
    }

    //return second in checkSpiResponseForTransactionDownloadResponse in else cases
    private ResponseObject<Xs2aTransactionsDownloadResponse> buildResponseObjectTransactionsDownloadResponseWithSpiResponseOtherError(String consentId, String accountId, String downloadId, SpiResponse<SpiTransactionsDownloadResponse> spiResponse) {
        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
        log.info("X-Request-ID: [{}], Consent-ID [{}], Account-ID: [{}], Download-ID: [{}]. Download transactions failed: couldn't get download transactions stream by link.",
                 requestProviderService.getRequestId(), consentId, accountId, downloadId);
        return ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                   .fail(new MessageError(errorHolder))
                   .build();
    }

    // fourth call in getTransactionsReportByPeriod
    @NotNull
    private ResponseObject<Xs2aTransactionsReport> getTransactionsReportByPeriodSuccess(Xs2aTransactionsReportByPeriodRequest request,
                                                                                        AccountConsent accountConsent,
                                                                                        SpiTransactionReport spiTransactionReport) {
        Xs2aAccountReport report = transactionsToAccountReportMapper
                                       .mapToXs2aAccountReport(spiTransactionReport.getTransactions(),
                                                               spiTransactionReport.getTransactionsRaw())
                                       .orElse(null);

        Xs2aTransactionsReport transactionsReport = getXs2aTransactionsReport(report,
                                                                              getRequestedAccountReference(accountConsent, request.getAccountId()),
                                                                              spiTransactionReport);
        if (spiTransactionReport.getDownloadId() != null) {
            String encodedDownloadId = Base64.getUrlEncoder().encodeToString(spiTransactionReport.getDownloadId().getBytes());
            transactionsReport.setDownloadId(encodedDownloadId);
        }

        return buildResponseObjectTransactionsReport(request, accountConsent, transactionsReport);
    }

    // returned fourth in getTransactionsReportByPeriodSuccess
    @NotNull
    private ResponseObject<Xs2aTransactionsReport> buildResponseObjectTransactionsReport(Xs2aTransactionsReportByPeriodRequest request,
                                                                                         AccountConsent accountConsent,
                                                                                         Xs2aTransactionsReport transactionsReport) {
        ResponseObject<Xs2aTransactionsReport> response =
            ResponseObject.<Xs2aTransactionsReport>builder()
                .body(transactionsReport)
                .build();

        aisConsentService.consentActionLog(tppService.getTppId(),
                                           request.getConsentId(),
                                           accountHelperService.createActionStatus(request.isWithBalance(), response),
                                           request.getRequestUri(),
                                           accountHelperService.needsToUpdateUsage(accountConsent));
        return response;
    }

    // used in getTransactionsReportByPeriodSuccess
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

    // used in getSpiResponseSpiTransactionReport, getSpiResponseSpiTransaction, getTransactionsReportByPeriodSuccess
    private SpiAccountReference getRequestedAccountReference(AccountConsent accountConsent, String accountId) {
        Xs2aAccountAccess access = accountConsent.getAccess();
        return accountHelperService.findAccountReference(access.getAllPsd2(), access.getTransactions(), accountId);
    }
}
