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
import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.aspsp.api.config.CmsAspspApiTagName;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static de.adorsys.psd2.consent.aspsp.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;

@RequestMapping(path = "aspsp-api/v1/pis/payments")
@Api(value = "aspsp-api/v1/pis/payments", tags = CmsAspspApiTagName.ASPSP_EXPORT_PAYMENTS)
public interface CmsAspspPisExportApi {
    Logger log = LoggerFactory.getLogger(CmsAspspPisExportApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @GetMapping(path = "/tpp/{tpp-id}")
    @ApiOperation(value = "Returns a list of payments by given mandatory TPP ID, optional creation date, PSU ID Data and instance ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    default ResponseEntity<Collection<CmsPayment>> _getPaymentsByTpp(
        @ApiParam(value = "TPP ID", required = true, example = "12345987")
        @PathVariable("tpp-id") String tppId,
        @ApiParam(value = "Creation start date", example = "2010-01-01")
        @RequestHeader(value = "start-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @ApiParam(value = "Creation end date", example = "2030-01-01")
        @RequestHeader(value = "end-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's" +
            " documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @ApiParam(value = "ID of the particular service instance")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return getPaymentsByTpp(tppId, start, end, psuId, psuIdType, psuCorporateId, psuCorporateIdType, instanceId);
    }

    // Override this method
    default ResponseEntity<Collection<CmsPayment>> getPaymentsByTpp(String tppId, LocalDate start, LocalDate end, String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsAspspPisExportApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/psu")
    @ApiOperation(value = "Returns a list of payments by given mandatory PSU ID Data, optional creation date and instance ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    default ResponseEntity<Collection<CmsPayment>> _getPaymentsByPsu(
        @ApiParam(value = "Creation start date", example = "2010-01-01")
        @RequestHeader(value = "start-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @ApiParam(value = "Creation end date", example = "2030-01-01")
        @RequestHeader(value = "end-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @ApiParam(value = "ID of the particular service instance")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return getPaymentsByPsu(start, end, psuId, psuIdType, psuCorporateId, psuCorporateIdType, instanceId);
    }

    // Override this method
    default ResponseEntity<Collection<CmsPayment>> getPaymentsByPsu(LocalDate start, LocalDate end, String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsAspspPisExportApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(path = "/account/{account-id}")
    @ApiOperation(value = "Returns a list of payments by given mandatory aspsp account id, optional creation date and instance ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    default ResponseEntity<Collection<CmsPayment>> _getPaymentsByAccountId(
        @ApiParam(value = "Bank specific account identifier.", required = true, example = "11111-99999")
        @PathVariable("account-id") String aspspAccountId,
        @ApiParam(value = "Creation start date", example = "2010-01-01")
        @RequestHeader(value = "start-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @ApiParam(value = "Creation end date", example = "2030-01-01")
        @RequestHeader(value = "end-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
        @ApiParam(value = "ID of the particular service instance")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return getPaymentsByAccountId(aspspAccountId, start, end, instanceId);
    }

    // Override this method
    default ResponseEntity<Collection<CmsPayment>> getPaymentsByAccountId(String aspspAccountId, LocalDate start, LocalDate end, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsAspspPisExportApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
