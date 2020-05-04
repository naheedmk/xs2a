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
import de.adorsys.psd2.consent.api.CmsConstant;
import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentAccessRequest;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisPsuDataAuthorisation;
import de.adorsys.psd2.consent.psu.api.config.CmsPsuApiTagName;
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

@RequestMapping(path = "psu-api/v1/ais/consent")
@Api(value = "psu-api/v1/ais/consent", tags = CmsPsuApiTagName.PSU_AIS_CONSENTS)
public interface CmsPsuAisApi {
    Logger log = LoggerFactory.getLogger(CmsPsuAisApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @PutMapping(path = "/{consent-id}/authorisation/{authorisation-id}/psu-data")
    @ApiOperation(value = "Updates PSU Data in consent, based on the trusted information about PSU known to ASPSP (i.e. after authorisation).")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsAisConsentResponse.class)
    })
    default ResponseEntity<Object> _updatePsuDataInConsent(
        @SuppressWarnings("unused")
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID, value = "The consent identifier", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @ApiParam(name = CmsConstant.PATH.AUTHORISATION_ID, value = "The authorisation identifier of the current authorisation session", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable(CmsConstant.PATH.AUTHORISATION_ID) String authorisationId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @RequestBody PsuIdData psuIdData) {
        return updatePsuDataInConsent(consentId, authorisationId, instanceId, psuIdData);
    }

    // Override this method
    default ResponseEntity<Object> updatePsuDataInConsent(String consentId, String authorisationId, String instanceId, PsuIdData psuIdData) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuAisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/{consent-id}/authorisation/{authorisation-id}/status/{status}")
    @ApiOperation(value = "Updates a Status of AIS Consent Authorisation by its ID and PSU ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsAisConsentResponse.class)})
    @PsuHeadersDescription
    default ResponseEntity<Object> _updateAuthorisationStatus(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @ApiParam(value = "The following code values are permitted 'received', 'psuIdentified', 'psuAuthenticated', 'scaMethodSelected', 'started', 'finalised', 'failed', 'exempted', 'unconfirmed'. These values might be extended by ASPSP by more values.",
            allowableValues = "RECEIVED, PSUIDENTIFIED, PSUAUTHENTICATED, SCAMETHODSELECTED,  STARTED,  FINALISED, FAILED, EXEMPTED, UNCONFIRMED",
            required = true)
        @PathVariable(CmsConstant.PATH.STATUS) String status,
        @ApiParam(name = CmsConstant.PATH.AUTHORISATION_ID,
            value = "The consent authorisation identification assigned to the created authorisation.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.AUTHORISATION_ID) String authorisationId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID, required = false) String psuId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID_TYPE, required = false) String psuIdType,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID, required = false) String psuCorporateId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID_TYPE, required = false) String psuCorporateIdType,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @RequestBody(required = false) AuthenticationDataHolder authenticationDataHolder) {
        return updateAuthorisationStatus(consentId, status, authorisationId, psuId, psuIdType, psuCorporateId, psuCorporateIdType, instanceId, authenticationDataHolder);
    }

    // Override this method
    default ResponseEntity<Object> updateAuthorisationStatus(String consentId, String status, String authorisationId, String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId, AuthenticationDataHolder authenticationDataHolder) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuAisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/{consent-id}/confirm-consent")
    @ApiOperation(value = "Puts a Status of AIS Consent object by its ID and PSU ID to VALID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<Boolean> _confirmConsent(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return confirmConsent(consentId, instanceId);
    }

    // Override this method
    default ResponseEntity<Boolean> confirmConsent(String consentId, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuAisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/{consent-id}/reject-consent")
    @ApiOperation(value = "Puts a Status of AIS Consent object by its ID and PSU ID to REJECTED")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<Boolean> _rejectConsent(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return rejectConsent(consentId, instanceId);
    }

    // Override this method
    default ResponseEntity<Boolean> rejectConsent(String consentId, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuAisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/consents")
    @ApiOperation(value = "Returns a list of AIS Consent objects by PSU ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    @PsuHeadersDescription
    default ResponseEntity<List<CmsAisAccountConsent>> _getConsentsForPsu(
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID, required = false) String psuId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID_TYPE, required = false) String psuIdType,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID, required = false) String psuCorporateId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID_TYPE, required = false) String psuCorporateIdType,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return getConsentsForPsu(psuId, psuIdType, psuCorporateId, psuCorporateIdType, instanceId);
    }

    // Override this method
    default ResponseEntity<List<CmsAisAccountConsent>> getConsentsForPsu(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuAisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/{consent-id}/revoke-consent")
    @ApiOperation(value = "Revokes AIS Consent object by its ID. Consent gets status Revoked by PSU.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<Boolean> _revokeConsent(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return revokeConsent(consentId, instanceId);
    }

    // Override this method
    default ResponseEntity<Boolean> revokeConsent(String consentId, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuAisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/{consent-id}/authorise-partially-consent")
    @ApiOperation(value = "Puts a Status of AIS Consent object by its ID and PSU ID to PARTIALLY_AUTHORISED.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 404, message = "Not Found")})
    @PsuHeadersDescription
    default ResponseEntity<Boolean> _authorisePartiallyConsent(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return authorisePartiallyConsent(consentId, instanceId);
    }

    // Override this method
    default ResponseEntity<Boolean> authorisePartiallyConsent(String consentId, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuAisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/redirect/{redirect-id}")
    @ApiOperation(value = "Gets consent response by redirect ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsAisConsentResponse.class),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsAisConsentResponse.class)})
    default ResponseEntity<CmsAisConsentResponse> _getConsentIdByRedirectId(
        @ApiParam(name = CmsConstant.PATH.REDIRECT_ID, value = "The redirect identification assigned to the created consent", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable(CmsConstant.PATH.REDIRECT_ID) String redirectId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return getConsentIdByRedirectId(redirectId, instanceId);
    }

    // Override this method
    default ResponseEntity<CmsAisConsentResponse> getConsentIdByRedirectId(String redirectId, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuAisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/{consent-id}")
    @ApiOperation(value = "Returns AIS Consent object by its ID.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsAisAccountConsent.class),
        @ApiResponse(code = 404, message = "Not Found")})
    @PsuHeadersDescription
    default ResponseEntity<CmsAisAccountConsent> _getConsentByConsentId(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID, required = false) String psuId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID_TYPE, required = false) String psuIdType,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID, required = false) String psuCorporateId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID_TYPE, required = false) String psuCorporateIdType,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return getConsentByConsentId(consentId, psuId, psuIdType, psuCorporateId, psuCorporateIdType, instanceId);
    }

    // Override this method
    default ResponseEntity<CmsAisAccountConsent> getConsentByConsentId(String consentId, String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuAisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "authorisation/{authorisation-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsPsuAuthorisation.class),
        @ApiResponse(code = 400, message = "Bad request")})
    default ResponseEntity<CmsPsuAuthorisation> _getAuthorisationByAuthorisationId(
        @ApiParam(name = CmsConstant.PATH.AUTHORISATION_ID,
            value = "The authorisation identification.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.AUTHORISATION_ID) String authorisationId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return getAuthorisationByAuthorisationId(authorisationId, instanceId);
    }

    // Override this method
    default ResponseEntity<CmsPsuAuthorisation> getAuthorisationByAuthorisationId(String authorisationId, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuAisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/{consent-id}/save-access")
    @ApiOperation(value = "Stores list of accounts with their identifiers in AIS Consent object by its ID. Consent should not be revoked, cancelled or expired.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", reference = "Access saved"),
        @ApiResponse(code = 404, message = "Not Found", reference = "Consent not found or not active")})
    default ResponseEntity<Void> _putAccountAccessInConsent(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestBody CmsAisConsentAccessRequest accountAccessRequest,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return putAccountAccessInConsent(consentId, accountAccessRequest, instanceId);
    }

    // Override this method
    default ResponseEntity<Void> putAccountAccessInConsent(String consentId, CmsAisConsentAccessRequest accountAccessRequest, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuAisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/{consent-id}/authorisation/psus")
    @ApiOperation(value = "Returns list of info objects about psu data and authorisation scaStatuses")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsAisPsuDataAuthorisation.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<List<CmsAisPsuDataAuthorisation>> _psuDataAuthorisations(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The consent identification assigned to the created consent authorization.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {

        return psuDataAuthorisations(consentId, instanceId);
    }

    // Override this method
    default ResponseEntity<List<CmsAisPsuDataAuthorisation>> psuDataAuthorisations(String consentId, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuAisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
