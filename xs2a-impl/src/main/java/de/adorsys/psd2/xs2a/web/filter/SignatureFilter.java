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

import de.adorsys.psd2.validator.certificate.CertificateErrorMsgCode;
import de.adorsys.psd2.validator.signature.TppSignatureValidator;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.request.RequestPathResolver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Component
public class SignatureFilter extends AbstractXs2aFilter {
    private final AspspProfileServiceWrapper aspspProfileService;
    private final RequestProviderService requestProviderService;

    public SignatureFilter(RequestPathResolver requestPathResolver,
                           AspspProfileServiceWrapper aspspProfileService,
                           RequestProviderService requestProviderService) {
        super(requestPathResolver);
        this.aspspProfileService = aspspProfileService;
        this.requestProviderService = requestProviderService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (!aspspProfileService.getTppSignatureRequired()) {
            chain.doFilter(request, response);
            return;
        }

        String signature = request.getHeader("signature");
        String errText;

        if (StringUtils.isBlank(signature)) {
            errText = CertificateErrorMsgCode.SIGNATURE_MISSING.toString();
            log.info("InR-ID: [{}], X-Request-ID: [{}]. TPP unauthorized: {}",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), errText);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, errText);
            return;
        }

        if (digestContainsErrors(request)) {
            errText = CertificateErrorMsgCode.FORMAT_ERROR.toString();
            log.info("InR-ID: [{}], X-Request-ID: [{}], TPP unauthorized: {}",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), errText);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, errText);
            return;
        }

        Map<String, String> headers = obtainRequestHeaders(request);
        String encodedTppCert = request.getHeader("tpp-signature-certificate");
        TppSignatureValidator tppSignatureValidator = new TppSignatureValidator();

        if (tppSignatureValidator.verifySignature(signature, encodedTppCert, headers)) {
            chain.doFilter(request, response);
        } else {
            errText = CertificateErrorMsgCode.SIGNATURE_INVALID.toString();
            log.info("InR-ID: [{}], X-Request-ID: [{}], TPP unauthorized: {}",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), errText);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, errText);
        }
    }

    private boolean digestContainsErrors(HttpServletRequest httpRequest) {
        String digest = httpRequest.getHeader("digest");
        return StringUtils.isBlank(digest) || !Arrays.asList(64, 128).contains(digest.getBytes().length);
    }

    private Map<String, String> obtainRequestHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                   .collect(Collectors.toMap(Function.identity(), request::getHeader));
    }
}
