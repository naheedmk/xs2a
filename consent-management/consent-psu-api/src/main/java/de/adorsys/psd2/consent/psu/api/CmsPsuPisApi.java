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

package de.adorsys.psd2.consent.psu.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.api.pis.CmsPaymentResponse;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.psu.api.config.CmsPsuApiTagName;
import de.adorsys.psd2.consent.psu.api.pis.CmsPisPsuDataAuthorisation;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.consent.psu.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;

@RequestMapping(path = "psu-api/v1/payment")
@Api(value = "psu-api/v1/payment", tags = CmsPsuApiTagName.PSU_PIS_PAYMENT)
public interface CmsPsuPisApi {
    Logger log = LoggerFactory.getLogger(CmsPsuPisApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @PutMapping(path = "/authorisation/{authorisation-id}/psu-data")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CreatePisCommonPaymentResponse.class),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsPaymentResponse.class)})
    default ResponseEntity<Object> _updatePsuInPayment(
        @ApiParam(name = "authorisation-id",
            value = "The authorisation's identifier",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @RequestBody PsuIdData psuIdData) {
        return updatePsuInPayment(authorisationId, instanceId, psuIdData);
    }

    // Override this method
    default ResponseEntity<Object> updatePsuInPayment(String authorisationId, String instanceId, PsuIdData psuIdData) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuPisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/redirect/{redirect-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsPaymentResponse.class),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsPaymentResponse.class)})
    default ResponseEntity<Object> _getPaymentIdByRedirectId(
        @ApiParam(name = "redirect-id",
            value = "The redirect identification assigned to the created payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("redirect-id") String redirectId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return getPaymentIdByRedirectId(redirectId, instanceId);
    }

    // Override this method
    default ResponseEntity<Object> getPaymentIdByRedirectId(String redirectId, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuPisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/{payment-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsPayment.class),
        @ApiResponse(code = 400, message = "Bad request")})
    default ResponseEntity<CmsPayment> _getPaymentByPaymentId(
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return getPaymentByPaymentId(psuId, psuIdType, psuCorporateId, psuCorporateIdType, paymentId, instanceId);
    }

    // Override this method
    default ResponseEntity<CmsPayment> getPaymentByPaymentId(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String paymentId, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuPisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/cancellation/redirect/{redirect-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsPaymentResponse.class),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsPaymentResponse.class)})
    default ResponseEntity<Object> _getPaymentIdByRedirectIdForCancellation(
        @ApiParam(name = "redirect-id",
            value = "The redirect identification assigned to the created payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("redirect-id") String redirectId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return getPaymentIdByRedirectIdForCancellation(redirectId, instanceId);
    }

    // Override this method
    default ResponseEntity<Object> getPaymentIdByRedirectIdForCancellation(String redirectId, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuPisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/cancellation/{payment-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsPayment.class),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<CmsPayment> _getPaymentByPaymentIdForCancellation(
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @ApiParam(name = "payment-id",
            value = "The payment identification.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return getPaymentByPaymentIdForCancellation(psuId, psuIdType, psuCorporateId, psuCorporateIdType, paymentId, instanceId);
    }

    // Override this method
    default ResponseEntity<CmsPayment> getPaymentByPaymentIdForCancellation(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String paymentId, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuPisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "authorisation/{authorisation-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsPsuAuthorisation.class),
        @ApiResponse(code = 400, message = "Bad request")})
    default ResponseEntity<CmsPsuAuthorisation> _getAuthorisationByAuthorisationId(
        @ApiParam(name = "authorisation-id",
            value = "The authorisation identification.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return getAuthorisationByAuthorisationId(authorisationId, instanceId);
    }

    // Override this method
    default ResponseEntity<CmsPsuAuthorisation> getAuthorisationByAuthorisationId(String authorisationId, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuPisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/{payment-id}/authorisation/{authorisation-id}/status/{status}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsPaymentResponse.class)})
    default ResponseEntity<Object> _updateAuthorisationStatus(
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @ApiParam(name = "authorisation-id",
            value = "The payment authorisation identification assigned to the created payment authorisation.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @ApiParam(value = "The following code values are permitted 'received', 'psuIdentified', 'psuAuthenticated', 'scaMethodSelected', 'started', 'finalised', 'failed', 'exempted', 'unconfirmed'. These values might be extended by ASPSP by more values.",
            allowableValues = "RECEIVED, PSUIDENTIFIED, PSUAUTHENTICATED, SCAMETHODSELECTED,  STARTED,  FINALISED, FAILED, EXEMPTED, UNCONFIRMED",
            required = true)
        @PathVariable("status") String status,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @RequestBody(required = false) AuthenticationDataHolder authenticationDataHolder) {
        return updateAuthorisationStatus(psuId, psuIdType, psuCorporateId,psuCorporateIdType, paymentId,authorisationId, status,instanceId,authenticationDataHolder);
    }

    // Override this method
    default ResponseEntity<Object> updateAuthorisationStatus(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String paymentId, String authorisationId, String status, String instanceId, AuthenticationDataHolder authenticationDataHolder) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuPisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/{payment-id}/status/{status}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")})
    default ResponseEntity<Void> _updatePaymentStatus(
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @ApiParam(value = "The following code values are permitted 'ACCC', 'ACCP', 'ACSC', 'ACSP', 'ACTC', 'ACWC', 'ACWP', 'PDNG', 'RJCT', 'RCVD', 'CANC', 'ACFC', 'PATC'. These values might be extended by ASPSP by more values.",
            allowableValues = "ACCC, ACCP, ACSC, ACSP, ACTC, ACWC, ACWP, RCVD, PDNG, RJCT, CANC, ACFC, PATC",
            required = true)
        @PathVariable("status") String status,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return updatePaymentStatus(paymentId, status, instanceId);
    }

    // Override this method
    default ResponseEntity<Void> updatePaymentStatus(String paymentId, String status, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuPisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/{payment-id}/authorisation/psus")
    @ApiOperation(value = "Returns list of info objects about PSU data and authorisation statuses")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsPisPsuDataAuthorisation.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<List<CmsPisPsuDataAuthorisation>> _psuAuthorisationStatuses(
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return psuAuthorisationStatuses(paymentId, instanceId);
    }

    // Override this method
    default ResponseEntity<List<CmsPisPsuDataAuthorisation>> psuAuthorisationStatuses(String paymentId, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuPisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
