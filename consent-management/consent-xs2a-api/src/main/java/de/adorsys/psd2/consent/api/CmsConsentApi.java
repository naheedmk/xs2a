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
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.ais.ConsentStatusResponse;
import de.adorsys.psd2.consent.api.config.InternalCmsXs2aApiTagName;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RequestMapping(path = "api/v1/consent")
@Api(value = "api/v1/consent", tags = InternalCmsXs2aApiTagName.CONSENTS)
public interface CmsConsentApi {
    Logger log = LoggerFactory.getLogger(CmsConsentApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @PostMapping
    @ApiOperation(value = "Create new consent")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = String.class),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 204, message = "No Content")})
    default ResponseEntity<Object> _createConsent(@RequestBody CmsConsent request) {
        return createConsent(request);
    }

    // Override this method
    default ResponseEntity<Object> createConsent(CmsConsent request) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsConsentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/{encrypted-consent-id}")
    @ApiOperation(value = "Read consent by ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsConsent.class),
        @ApiResponse(code = 404, message = "Not found")})
    default ResponseEntity<CmsConsent> _getConsentById(
        @ApiParam(name = "encrypted-consent-id",
            value = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId) {
        return getConsentById(encryptedConsentId);
    }

    // Override this method
    default ResponseEntity<CmsConsent> getConsentById(String encryptedConsentId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsConsentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/{encrypted-consent-id}/status")
    @ApiOperation(value = "Get consent status by ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ConsentStatus.class),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<ConsentStatusResponse> _getConsentStatusById(
        @ApiParam(name = "encrypted-consent-id",
            value = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId) {
        return getConsentStatusById(encryptedConsentId);
    }

    // Override this method
    default ResponseEntity<ConsentStatusResponse> getConsentStatusById(String encryptedConsentId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsConsentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/{encrypted-consent-id}/status/{status}")
    @ApiOperation(value = "Update consent status by ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<Object> _updateConsentStatus(
        @ApiParam(name = "encrypted-consent-id",
            value = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId,
        @ApiParam(value = "The following code values are permitted 'VALID', 'REJECTED', 'REVOKED_BY_PSU', 'TERMINATED_BY_TPP'. These values might be extended by ASPSP by more values.",
            example = "VALID",
            required = true)
        @PathVariable("status") String status) {
        return updateConsentStatus(encryptedConsentId, status);
    }

    // Override this method
    default ResponseEntity<Object> updateConsentStatus(String encryptedConsentId, String status) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsConsentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @DeleteMapping(path = "/{encrypted-consent-id}/old-consents")
    @ApiOperation(value = "Find and terminate old consents for TPP and PSU by new consent ID")
    @ApiResponse(code = 204, message = "No Content")
    default ResponseEntity<Void> _findAndTerminateOldConsentsByNewConsentId(
        @ApiParam(name = "encrypted-consent-id",
            value = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId) {
        return findAndTerminateOldConsentsByNewConsentId(encryptedConsentId);
    }

    // Override this method
    default ResponseEntity<Void> findAndTerminateOldConsentsByNewConsentId(String encryptedConsentId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsConsentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/{encrypted-consent-id}/multilevel-sca")
    @ApiOperation(value = "Update requirement for multilevel SCA for consent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<Object> _updateMultilevelScaRequired(
        @ApiParam(name = "encrypted-consent-id", value = "Encrypted consent ID", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId,
        @ApiParam(name = "multilevel-sca", value = "Multilevel SCA.", example = "false")
        @RequestParam(value = "multilevel-sca", defaultValue = "false") boolean multilevelSca) {
        return updateMultilevelScaRequired(encryptedConsentId, multilevelSca);
    }

    // Override this method
    default ResponseEntity<Object> updateMultilevelScaRequired(String encryptedConsentId, boolean multilevelSca) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsConsentApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
