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
import de.adorsys.psd2.consent.api.piis.CmsPiisConsent;
import de.adorsys.psd2.consent.psu.api.config.CmsPsuApiTagName;
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

@RequestMapping(path = "psu-api/v1/piis/consents")
@Api(value = "psu-api/v1/piis/consents", tags = CmsPsuApiTagName.PSU_PIIS_CONSENTS)
public interface CmsPsuPiisApi {
    Logger log = LoggerFactory.getLogger(CmsPsuPiisApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @GetMapping(path = "/{consent-id}")
    @ApiOperation(value = "Returns PIIS Consent object by its ID.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsPiisConsent.class),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<CmsPiisConsent> _getConsent(
        @ApiParam(name = "consent-id",
            value = "PIIS consent identification assigned to the created PIIS consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("consent-id") String consentId,
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding PIIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return getConsent(consentId, psuId, psuIdType, psuCorporateId, psuCorporateIdType, instanceId);
    }

    // Override this method
    default ResponseEntity<CmsPiisConsent> getConsent(String consentId, String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuPiisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

        @GetMapping
        @ApiOperation(value = "Returns a list of PIIS Consent objects by PSU ID")
        @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK")})
        default ResponseEntity<List<CmsPiisConsent>> _getConsentsForPsu(
            @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding PIIS service in the same session. ")
            @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return getConsentsForPsu(psuId, psuIdType, psuCorporateId, psuCorporateIdType, instanceId);
    }

    // Override this method
    default ResponseEntity<List<CmsPiisConsent>> getConsentsForPsu(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuPiisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/{consent-id}/revoke-consent")
    @ApiOperation(value = "Revokes PIIS Consent object by its ID. Consent gets status Revoked by PSU.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class)})
    default ResponseEntity<Boolean> _revokeConsent(
        @ApiParam(name = "consent-id",
            value = "PIIS consent identification assigned to the created PIIS consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("consent-id") String consentId,
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding PIIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return revokeConsent(consentId, psuId, psuIdType, psuCorporateId, psuCorporateIdType, instanceId);
    }

    // Override this method
    default ResponseEntity<Boolean> revokeConsent(String consentId, String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsPsuPiisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
