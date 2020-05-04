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
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RequestMapping(path = "api/v1/tpp")
@Api(value = "api/v1/tpp", tags = InternalCmsXs2aApiTagName.TPP)
public interface TppApi {
    Logger log = LoggerFactory.getLogger(TppApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @PutMapping
    @ApiOperation(value = "Updates TPP Info")
    @ApiResponse(code = 200, message = "OK")
    default ResponseEntity<Boolean> _updateTppInfo(@RequestBody TppInfo tppInfo) {
        return updateTppInfo(tppInfo);
    }

    // Override this method
    default ResponseEntity<Boolean> updateTppInfo(TppInfo tppInfo) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default TppApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/stop-list")
    @ApiOperation(value = "Checks if TPP is blocked")
    @ApiResponse(code = 200, message = "OK")
    default ResponseEntity<Boolean> _checkIfTppBlocked(
        @ApiParam(value = "ID of TPP", example = "12345987")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber) {
        return checkIfTppBlocked(tppAuthorisationNumber);
    }

    // Override this method
    default ResponseEntity<Boolean> checkIfTppBlocked(String tppAuthorisationNumber) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default TppApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
