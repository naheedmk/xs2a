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

package de.adorsys.psd2.consent.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.consent.api.authorisation.*;
import de.adorsys.psd2.consent.api.config.InternalCmsXs2aApiTagName;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentDataStatusResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RequestMapping(path = "api/v1/pis/common-payments")
@Api(value = "api/v1/pis/common-payments", tags = InternalCmsXs2aApiTagName.PIS_COMMON_PAYMENT)
public interface PisCommonPaymentApi {
    Logger log = LoggerFactory.getLogger(PisCommonPaymentApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @PostMapping(path = "/")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CreatePisCommonPaymentResponse.class),
        @ApiResponse(code = 400, message = "Bad request")})
    default ResponseEntity<CreatePisCommonPaymentResponse> _createCommonPayment(@RequestBody PisPaymentInfo request) {
        return createCommonPayment(request);
    }

    // Override this method
    default ResponseEntity<CreatePisCommonPaymentResponse> createCommonPayment(PisPaymentInfo request) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/{payment-id}/status")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = PisCommonPaymentDataStatusResponse.class),
        @ApiResponse(code = 400, message = "Bad request")})
    default ResponseEntity<PisCommonPaymentDataStatusResponse> _getPisCommonPaymentStatusById(
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId) {
        return getPisCommonPaymentStatusById(paymentId);
    }

    // Override this method
    default ResponseEntity<PisCommonPaymentDataStatusResponse> getPisCommonPaymentStatusById(String paymentId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/{payment-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = PisCommonPaymentResponse.class),
        @ApiResponse(code = 400, message = "Bad request")})
    default ResponseEntity<PisCommonPaymentResponse> _getCommonPaymentById(
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId) {
        return getCommonPaymentById(paymentId);
    }

    // Override this method
    default ResponseEntity<PisCommonPaymentResponse> getCommonPaymentById(String paymentId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/{payment-id}/status/{status}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad request")})
    default ResponseEntity<Void> _updateCommonPaymentStatus(
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @ApiParam(value = "The following code values are permitted 'ACCC', 'ACCP', 'ACSC', 'ACSP', 'ACTC', 'ACWC', 'ACWP', 'PDNG', 'RJCT', 'RCVD', 'CANC', 'ACFC', 'PATC'. These values might be extended by ASPSP by more values.",
            allowableValues = "AcceptedSettlementCompletedCreditor, AcceptedCustomerProfile, AcceptedSettlementCompleted, AcceptedSettlementInProcess, AcceptedTechnicalValidation, AcceptedWithChange, AcceptedWithoutPosting, Received, Pending, Rejected, Canceled, AcceptedFundsChecked, PartiallyAcceptedTechnicalCorrect",
            required = true)
        @PathVariable("status") String status) {
        return updateCommonPaymentStatus(paymentId, status);
    }

    // Override this method
    default ResponseEntity<Void> updateCommonPaymentStatus(String paymentId, String status) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping(path = "/{payment-id}/authorisations")
    @ApiOperation(value = "Create authorisation for given id.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<CreateAuthorisationResponse> _createAuthorisation(
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created authorisation.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @RequestBody CreateAuthorisationRequest request) {
        return createAuthorisation(paymentId, request);
    }

    // Override this method
    default ResponseEntity<CreateAuthorisationResponse> createAuthorisation(String paymentId, CreateAuthorisationRequest request) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping(path = "/{payment-id}/cancellation-authorisations")
    @ApiOperation(value = "Create payment authorisation cancellation for given payment id.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<CreateAuthorisationResponse> _createAuthorisationCancellation(
        @ApiParam(name = "payment-id",
            value = "The payment identification of the related payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @RequestBody CreateAuthorisationRequest request) {
        return createAuthorisationCancellation(paymentId, request);
    }

    // Override this method
    default ResponseEntity<CreateAuthorisationResponse> createAuthorisationCancellation(String paymentId, CreateAuthorisationRequest request) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/authorisations/{authorisation-id}")
    @ApiOperation(value = "Update pis authorisation.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<Authorisation> _updateAuthorisation(
        @ApiParam(name = "authorisation-id",
            value = "The authorisation identification assigned to the created authorisation.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @RequestBody UpdateAuthorisationRequest request) {
        return updateAuthorisation(authorisationId, request);
    }

    // Override this method
    default ResponseEntity<Authorisation> updateAuthorisation(String authorisationId, UpdateAuthorisationRequest request) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "authorisations/{authorisation-id}/status/{status}")
    @ApiOperation(value = "Update status for PIS authorisation.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")
    })
    default ResponseEntity<Void> _updateAuthorisationStatus(
        @ApiParam(name = "authorisation-id",
            value = "The authorisation identification assigned to the created authorisation.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @ApiParam(name = "status",
            value = "The authorisation status.",
            example = "ScaStatus.FAILED",
            required = true)
        @PathVariable("status") String authorisationStatus) {
        return updateAuthorisationStatus(authorisationId, authorisationStatus);
    }

    // Override this method
    default ResponseEntity<Void> updateAuthorisationStatus(String authorisationId, String authorisationStatus) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/authorisations/{authorisation-id}")
    @ApiOperation(value = "Getting pis authorisation.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<Authorisation> _getAuthorisation(
        @ApiParam(name = "authorisation-id",
            value = "The authorisation identification assigned to the created authorisation.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId) {
        return getAuthorisation(authorisationId);
    }

    // Override this method
    default ResponseEntity<Authorisation> getAuthorisation(String authorisationId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/{payment-id}/authorisations/{authorisation-id}/status")
    @ApiOperation(value = "Gets SCA status of pis consent authorisation.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<ScaStatus> _getAuthorisationScaStatus(
        @ApiParam(name = "payment-id",
            value = "Identification of the payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @ApiParam(name = "authorisation-id",
            value = "The consent authorisation identification",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId) {
        return getAuthorisationScaStatus(paymentId, authorisationId);
    }

    // Override this method
    default ResponseEntity<ScaStatus> getAuthorisationScaStatus(String paymentId, String authorisationId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/cancellation-authorisations/{authorisation-id}")
    @ApiOperation(value = "Update pis cancellation authorisation.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<Authorisation> _updateCancellationAuthorisation(
        @ApiParam(name = "cancellation-id",
            value = "The cancellation authorisation identification assigned to the created cancellation authorisation.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @RequestBody UpdateAuthorisationRequest request) {
        return updateCancellationAuthorisation(authorisationId, request);
    }

    // Override this method
    default ResponseEntity<Authorisation> updateCancellationAuthorisation(String authorisationId, UpdateAuthorisationRequest request) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/cancellation-authorisations/{authorisation-id}")
    @ApiOperation(value = "Getting pis cancellation authorisation.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<List<String>> _getAuthorisationCancellation(
        @ApiParam(name = "cancellation-id",
            value = "The cancellation authorisation identification assigned to the created cancellation authorisation.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId) {
        return getAuthorisationCancellation(authorisationId);
    }

    // Override this method
    default ResponseEntity<List<String>> getAuthorisationCancellation(String authorisationId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/{payment-id}/cancellation-authorisations")
    @ApiOperation(value = "Gets list of payment cancellation authorisation IDs by payment ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<List<String>> _getAuthorisationsCancellation(
        @ApiParam(name = "payment-id",
            value = "The payment identification of the related payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId) {
        return getAuthorisationsCancellation(paymentId);
    }

    // Override this method
    default ResponseEntity<List<String>> getAuthorisationsCancellation(String paymentId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/{payment-id}/cancellation-authorisations/{authorisation-id}/status")
    @ApiOperation(value = "Gets SCA status of pis consent cancellation authorisation.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<ScaStatus> _getCancellationAuthorisationScaStatus(
        @ApiParam(name = "payment-id",
            value = "Identification of the payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @ApiParam(name = "cancellation-id",
            value = "Identification of the consent cancellation authorisation",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId) {
        return getCancellationAuthorisationScaStatus(paymentId, authorisationId);
    }

    // Override this method
    default ResponseEntity<ScaStatus> getCancellationAuthorisationScaStatus(String paymentId, String authorisationId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/{payment-id}/authorisations")
    @ApiOperation(value = "Gets list of payment authorisation IDs by payment ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<List<String>> _getAuthorisations(
        @ApiParam(name = "payment-id",
            value = "The payment identification of the related payment.",
            example = "vOHy6fj2f5IgxHk-kTlhw6sZdTXbRE3bWsu2obq54beYOChP5NvRmfh06nrwumc2R01HygQenchEcdGOlU-U0A==_=_iR74m2PdNyE",
            required = true)
        @PathVariable("payment-id") String paymentId) {
        return getAuthorisations(paymentId);
    }

    // Override this method
    default ResponseEntity<List<String>> getAuthorisations(String paymentId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/authorisations/{authorisation-id}/authentication-methods/{authentication-method-id}")
    @ApiOperation(value = "Checks if requested authentication method is decoupled")
    @ApiResponse(code = 200, message = "OK")
    default ResponseEntity<Boolean> _isAuthenticationMethodDecoupled(
        @ApiParam(name = "authorisation-id",
            value = "Common payment authorisation identification",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @ApiParam(name = "authentication-method-id",
            value = "Authentication method identification",
            example = "sms",
            required = true)
        @PathVariable("authentication-method-id") String authenticationMethodId) {
    return isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }

    // Override this method
    default ResponseEntity<Boolean> isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping(path = "/authorisations/{authorisation-id}/authentication-methods")
    @ApiOperation(value = "Saves authentication methods in authorisation")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No Content"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<Void> _saveAuthenticationMethods(
        @ApiParam(name = "authorisation-id",
            value = "The common payment authorisation identification.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @RequestBody List<CmsScaMethod> methods) {
        return saveAuthenticationMethods(authorisationId, methods);
    }

    // Override this method
    default ResponseEntity<Void> saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/authorisations/{authorisation-id}/sca-approach/{sca-approach}")
    @ApiOperation(value = "Updates pis sca approach.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<Boolean> _updateScaApproach(
        @ApiParam(name = "authorisation-id",
            value = "The authorisation identification assigned to the created authorisation.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @ApiParam(name = "sca-approach",
            value = "Chosen SCA approach.",
            example = "REDIRECT",
            required = true)
        @PathVariable("sca-approach") ScaApproach scaApproach) {
        return updateScaApproach(authorisationId, scaApproach);
    }

    // Override this method
    default ResponseEntity<Boolean> updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/authorisations/{authorisation-id}/sca-approach")
    @ApiOperation(value = "Gets SCA approach of the payment initiation authorisation by its ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<AuthorisationScaApproachResponse> _getAuthorisationScaApproach(
        @ApiParam(name = "authorisation-id",
            value = "Identification of the payment initiation authorisation.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId) {
        return getAuthorisationScaApproach(authorisationId);
    }

    // Override this method
    default ResponseEntity<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/cancellation-authorisations/{authorisation-id}/sca-approach")
    @ApiOperation(value = "Gets SCA approach of the payment cancellation authorisation by its ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<AuthorisationScaApproachResponse> _getCancellationAuthorisationScaApproach(
        @ApiParam(name = "authorisation-id",
            value = "Identification of the payment cancellation authorisation.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId) {
        return getCancellationAuthorisationScaApproach(authorisationId);
    }

    // Override this method
    default ResponseEntity<AuthorisationScaApproachResponse> getCancellationAuthorisationScaApproach(String authorisationId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/{payment-id}/multilevel-sca")
    @ApiOperation(value = "Updates multilevel sca required by payment ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Bad Request")})
    default ResponseEntity<Boolean> _updateMultilevelScaRequired(
        @ApiParam(name = "payment-id",
            value = "The payment identification of the related payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(name = "payment-id") String paymentId,
        @ApiParam(name = "multilevel-sca", value = "Multilevel SCA.", example = "false")
        @RequestParam(value = "multilevel-sca", defaultValue = "false") boolean multilevelSca) {
        return updateMultilevelScaRequired(paymentId, multilevelSca);
    }

    // Override this method
    default ResponseEntity<Boolean> updateMultilevelScaRequired(String paymentId, boolean multilevelSca) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisCommonPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
