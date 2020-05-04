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
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static de.adorsys.psd2.consent.aspsp.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;

@RequestMapping(path = "aspsp-api/v1/pis/transaction-status")
@Api(value = "aspsp-api/v1/pis/transaction-status", tags = CmsAspspApiTagName.ASPSP_PIS_TRANSACTION_STATUS)
public interface CmsAspspPisTransactionApi {
    Logger log = LoggerFactory.getLogger(CmsAspspPisTransactionApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @PutMapping(path = "/{payment-id}/status/{status}")
    @ApiOperation(value = "Updated transaction status by payment id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad request")})
    default ResponseEntity<Void> _updatePaymentStatus(
        @ApiParam(name = "payment-id", value = "The payment identification assigned to the created payment.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable("payment-id") String paymentId,
        @ApiParam(value = "The following code values are permitted 'ACCC', 'ACCP', 'ACSC', 'ACSP', 'ACTC', 'ACWC', 'ACWP', 'PDNG', 'RJCT', 'RCVD', 'CANC', 'ACFC', 'PATC'. These values might be extended by ASPSP by more values.",
            allowableValues = "ACCC, ACCP, ACSC, ACSP, ACTC, ACWC, ACWP, RCVD, PDNG, RJCT, CANC, ACFC, PATC", required = true)
        @PathVariable("status") String status,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return updatePaymentStatus(paymentId, status, instanceId);
    }

    // Override this method
    default ResponseEntity<Void> updatePaymentStatus(String paymentId, String status, String instanceId) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default CmsAspspPisTransactionApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
