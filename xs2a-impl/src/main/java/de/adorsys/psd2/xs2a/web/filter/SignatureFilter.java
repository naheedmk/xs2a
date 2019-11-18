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

package de.adorsys.psd2.xs2a.web.filter;

import de.adorsys.psd2.validator.signature.DigestVerifier;
import de.adorsys.psd2.validator.signature.SignatureVerifier;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.exception.MessageCategory.ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignatureFilter extends AbstractXs2aFilter {
    private static final String PATTERN_MESSAGE = "InR-ID: [{}], X-Request-ID: [{}], TPP unauthorized: {}";
    private final AspspProfileServiceWrapper aspspProfileService;
    private final RequestProviderService requestProviderService;
    private final TppErrorMessageBuilder tppErrorMessageBuilder;
    private final DigestVerifier digestVerifier;
    private final SignatureVerifier signatureVerifier;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (!aspspProfileService.getTppSignatureRequired()) {
            chain.doFilter(request, response);
            return;
        }

        if (!validateHeadersExist(request, response)) {
            return;
        }

        String digest = request.getHeader("digest");
        String encodedCertificate = request.getHeader("tpp-signature-certificate");
        String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        boolean digestValid = digestVerifier.verify(digest, body);
        if (!digestValid) {
            String errorText = "Mandatory header 'digest' is invalid!";
            log.info(PATTERN_MESSAGE, requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(),
                     errorText);
            setResponseStatusAndErrorCode(response, FORMAT_ERROR);
            return;
        }

        Map<String, String> allHeaders = obtainRequestHeaders(request);
        String signature = request.getHeader("signature");
        String method = request.getMethod();
        String url = request.getRequestURL().toString();

        boolean signatureValid = signatureVerifier.verify(signature, encodedCertificate, allHeaders, method, url);
        if (!signatureValid) {
            String errorText = "Mandatory header 'signature' is invalid!";
            log.info(PATTERN_MESSAGE, requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(),
                     errorText);
            setResponseStatusAndErrorCode(response, SIGNATURE_INVALID);
            return;
        }

        chain.doFilter(request, response);
    }

    private Map<String, String> obtainRequestHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                   .collect(Collectors.toMap(Function.identity(), request::getHeader));
    }

    private boolean validateHeadersExist(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(request.getHeader("x-request-id"))) {
            String errorText = "Header 'x-request-id' is missing in request.";
            log.info("InR-ID: [{}], TPP unauthorized: {}", requestProviderService.getInternalRequestId(), errorText);
            setResponseStatusAndErrorCode(response, FORMAT_ERROR);
            return false;
        }

        if (StringUtils.isBlank(request.getHeader("signature"))) {
            String errorText = "Header 'signature' is missing in request.";
            log.info(PATTERN_MESSAGE, requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(),
                     errorText);
            setResponseStatusAndErrorCode(response, SIGNATURE_MISSING);
            return false;
        }

        StringBuilder errorMessages = new StringBuilder();
        if (StringUtils.isBlank(request.getHeader("tpp-signature-certificate"))) {
            errorMessages.append("Header 'tpp-signature-certificate' is missing in request.").append("\n");
        }

        if (StringUtils.isBlank(request.getHeader("digest"))) {
            errorMessages.append("Header 'digest' is missing in request.").append("\n");
        }

        if (StringUtils.isBlank(request.getHeader("date"))) {
            errorMessages.append("Header 'date' is missing in request.").append("\n");
        }

        if (errorMessages.length() > 0) {
            log.info(PATTERN_MESSAGE, requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(),
                     errorMessages.toString());
            setResponseStatusAndErrorCode(response, FORMAT_ERROR);
            return false;
        }

        return true;
    }

    private void setResponseStatusAndErrorCode(HttpServletResponse response, MessageErrorCode messageErrorCode) throws IOException {
        response.setStatus(messageErrorCode.getCode());
        response.getWriter().print(tppErrorMessageBuilder.buildTppErrorMessage(ERROR, messageErrorCode));
    }
}
