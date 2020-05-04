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
import de.adorsys.psd2.consent.api.config.InternalCmsXs2aApiTagName;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RequestMapping(path = "api/v1/pis")
@Api(value = "api/v1/pis", tags = InternalCmsXs2aApiTagName.PIS_PAYMENTS)
public interface PisPaymentApi {
    Logger log = LoggerFactory.getLogger(PisPaymentApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @GetMapping(path = "/payment/{payment-id}")
    @ApiOperation(value = "Get inner payment id by encrypted string")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<String> _getPaymentIdByEncryptedString(
        @ApiParam(name = "payment-id",
            value = "The payment identification.",
            example = "32454656712432",
            required = true)
        @PathVariable("payment-id") String encryptedId) {
        return getPaymentIdByEncryptedString(encryptedId);
    }

    // Override this method
    default ResponseEntity<String> getPaymentIdByEncryptedString(String encryptedId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/payment/{payment-id}/status/{status}")
    @ApiOperation(value = "Updates payment status after SPI service. Should not be used for any other purposes!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request")})
    default ResponseEntity<Void> _updatePaymentStatusAfterSpiService(
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @ApiParam(value = "The following code values are permitted 'ACCC', 'ACCP', 'ACSC', 'ACSP', 'ACTC', 'ACWC', 'ACWP', 'PDNG', 'RJCT', 'RCVD', 'CANC', 'ACFC', 'PATC'. These values might be extended by ASPSP by more values.",
            allowableValues = "ACCC, ACCP, ACSC, ACSP, ACTC, ACWC, ACWP, RCVD, PDNG, RJCT, CANC, ACFC, PATC",
            required = true)
        @PathVariable("status") String status) {
        return updatePaymentStatusAfterSpiService(paymentId, status);
    }

    // Override this method
    default ResponseEntity<Void> updatePaymentStatusAfterSpiService(String paymentId, String status) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/payment/{payment-id}/cancellation/redirects")
    @ApiOperation(value = "Updates payment cancellation redirect URIs after SPI service. Should not be used for any other purposes!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request")})
    default ResponseEntity<Void> _updatePaymentCancellationTppRedirectUri(
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @RequestHeader(value = "TPP-Redirect-URI", required = false) String tpPRedirectURI,
        @RequestHeader(value = "TPP-Nok-Redirect-URI", required = false) String tpPNokRedirectURI) {
        return updatePaymentCancellationTppRedirectUri(paymentId, tpPRedirectURI, tpPNokRedirectURI);
    }

    // Override this method
    default ResponseEntity<Void> updatePaymentCancellationTppRedirectUri(String paymentId, String tpPRedirectURI, String tpPNokRedirectURI) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/payment/{payment-id}/cancellation/internal-request-id/{internal-request-id}")
    @ApiOperation(value = "Updates payment cancellation internal request ID after SPI service. Should not be used for any other purposes!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request")})
    default ResponseEntity<Void> _updatePaymentCancellationInternalRequestId(
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @ApiParam(name = "internal-request-id",
            value = "Cancellation internal request ID of payment.",
            required = true)
        @PathVariable("internal-request-id") String internalRequestId) {
        return updatePaymentCancellationInternalRequestId(paymentId, internalRequestId);
    }

    // Override this method
    default ResponseEntity<Void> updatePaymentCancellationInternalRequestId(String paymentId, String internalRequestId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PisPaymentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
