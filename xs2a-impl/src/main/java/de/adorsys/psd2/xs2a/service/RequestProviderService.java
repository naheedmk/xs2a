/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.RequestData;
import de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestProviderService {

    private static final String TPP_REDIRECT_PREFERRED_HEADER = "tpp-redirect-preferred";
    private static final String X_REQUEST_ID_HEADER = "x-request-id";
    private static final String PSU_ID_HEADER = "psu-id";
    private static final String PSU_ID_TYPE_HEADER = "psu-id-type";
    private static final String PSU_CORPORATE_ID_HEADER = "psu-corporate-id";
    private static final String PSU_CORPORATE_ID_TYPE_HEADER = "psu-corporate-id-type";
    private static final String PSU_IP_ADDRESS = "psu-ip-address";
    private static final String TPP_ROLES_ALLOWED_HEADER = "tpp-roles-allowed";
    private static final String ACCEPT_HEADER = "accept";
    private static final String TPP_QWAC_CERTIFICATE_HEADER = "tpp-qwac-certificate";

    private final HttpServletRequest httpServletRequest;
    private final InternalRequestIdService internalRequestIdService;

    public Optional<Boolean> resolveTppRedirectPreferred() {

        String header = getHeader(TPP_REDIRECT_PREFERRED_HEADER);
        if (header == null) {
            return Optional.empty();
        }
        return Optional.of(Boolean.valueOf(header));
    }

    public RequestData getRequestData() {
        String uri = httpServletRequest.getRequestURI();
        UUID internalRequestId = getInternalRequestId();
        UUID requestId = UUID.fromString(getHeader(X_REQUEST_ID_HEADER));
        String ip = httpServletRequest.getRemoteAddr();
        Map<String, String> headers = getRequestHeaders(httpServletRequest);
        PsuIdData psuIdData = getPsuIdData();

        return new RequestData(uri, internalRequestId, requestId, ip, headers, psuIdData);
    }

    public PsuIdData getPsuIdData() {
        return new PsuIdData(getHeader(PSU_ID_HEADER),
                             getHeader(PSU_ID_TYPE_HEADER),
                             getHeader(PSU_CORPORATE_ID_HEADER),
                             getHeader(PSU_CORPORATE_ID_TYPE_HEADER),
                             getHeader(PSU_IP_ADDRESS));
    }

    /**
     * Returns internal request ID that was assigned to the current request
     * <p>
     * This ID is not provided by the TPP, instead it's being generated by the XS2A itself
     *
     * @return internal request ID
     */
    @NotNull
    public UUID getInternalRequestId() {
        return internalRequestIdService.getInternalRequestId();
    }

    public String getInternalRequestIdString() {
        return getInternalRequestId().toString();
    }

    public UUID getRequestId() {
        return getRequestData().getRequestId();
    }

    public String getRequestIdString() {
        return getHeader(X_REQUEST_ID_HEADER);
    }

    public boolean isRequestFromPsu() {
        return StringUtils.isNotBlank(getPsuIpAddress());
    }

    public boolean isRequestFromTPP() {
        return !isRequestFromPsu();
    }

    public String getPsuIpAddress() {
        return getHeader(Xs2aHeaderConstant.PSU_IP_ADDRESS);
    }

    public String getTppRedirectURI() {
        return getHeader(Xs2aHeaderConstant.TPP_REDIRECT_URI);
    }

    public String getTppNokRedirectURI() {
        return getHeader(Xs2aHeaderConstant.TPP_NOK_REDIRECT_URI);
    }

    public String getOAuth2Token() {

        String headerValue = getHeader(HttpHeaders.AUTHORIZATION);

        return StringUtils.isEmpty(headerValue)
                   ? null
                   : headerValue.replace("Bearer ", "");
    }

    public String getTppRolesAllowedHeader() {
        return getHeader(TPP_ROLES_ALLOWED_HEADER);
    }

    /**
     * Returns Accept header from the request. If the header is absent, returns any instead(*{@literal /}*)
     *
     * @return accept header
     */
    @NotNull
    public String getAcceptHeader() {
        String acceptHeader = getHeader(ACCEPT_HEADER);
        if (acceptHeader == null) {
            return MediaType.ALL_VALUE;
        }

        return acceptHeader;
    }

    public String getContentTypeHeader() {
        return getHeader(HttpHeaders.CONTENT_TYPE);
    }

    public String getEncodedTppQwacCert() {
        return getHeader(TPP_QWAC_CERTIFICATE_HEADER);
    }

    private String getHeader(String headerName) {
        return httpServletRequest.getHeader(headerName);
    }

    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames())
                   .stream()
                   .collect(Collectors.toMap(Function.identity(), request::getHeader));
    }
}
