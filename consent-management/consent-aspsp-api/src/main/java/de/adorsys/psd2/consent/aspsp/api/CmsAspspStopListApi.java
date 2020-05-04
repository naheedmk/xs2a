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

package de.adorsys.psd2.consent.aspsp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.consent.aspsp.api.config.CmsAspspApiTagName;
import de.adorsys.psd2.xs2a.core.tpp.TppStopListRecord;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static de.adorsys.psd2.consent.aspsp.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;

@RequestMapping(path = "aspsp-api/v1/tpp/stop-list")
@Api(value = "aspsp-api/v1/tpp/stop-list", tags = CmsAspspApiTagName.ASPSP_TPP_STOP_LIST)
public interface CmsAspspStopListApi {
    Logger log = LoggerFactory.getLogger(CmsAspspStopListApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @GetMapping
    @ApiOperation(value = "Returns TPP stop list record by TPP authorisation number")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    default ResponseEntity<TppStopListRecord> _getTppStopListRecord(
        @ApiParam(value = "ID of TPP", example = "12345987")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber,
        @ApiParam(value = "Service instance id", example = "instance id")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return getTppStopListRecord(tppAuthorisationNumber, instanceId);
    }

    // Override this method
    default ResponseEntity<TppStopListRecord> getTppStopListRecord(String tppAuthorisationNumber, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsAspspStopListApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(path = "/block")
    @ApiOperation(value = "Blocks TPP by TPP authorisation number and lock period")
    @ApiResponse(code = 200, message = "OK")
    default ResponseEntity<Boolean> _blockTpp(
        @ApiParam(value = "ID of TPP", example = "12345987")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber,
        @ApiParam(value = "Service instance id", example = "instance id")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @ApiParam(value = "Period of TPP locking (in milliseconds)", example = "1000")
        @RequestHeader(value = "lock-period", required = false) Long lockPeriod) {
        return blockTpp(tppAuthorisationNumber, instanceId, lockPeriod);
    }

    // Override this method
    default ResponseEntity<Boolean> blockTpp(String tppAuthorisationNumber, String instanceId, Long lockPeriod) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsAspspStopListApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @DeleteMapping(path = "/unblock")
    @ApiOperation(value = "Unblocks TPP by TPP authorisation number")
    @ApiResponse(code = 200, message = "OK")
    default ResponseEntity<Boolean> _unblockTpp(
        @ApiParam(value = "ID of TPP", example = "12345987")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber,
        @ApiParam(value = "Service instance id", example = "instance id")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return unblockTpp(tppAuthorisationNumber, instanceId);
    }

    // Override this method
    default ResponseEntity<Boolean> unblockTpp(String tppAuthorisationNumber, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsAspspStopListApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
