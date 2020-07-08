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

package de.adorsys.psd2.xs2a.web.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class MultiPartBoundaryBuilder {
    static final String DEFAULT_BOUNDARY = "--AaaBbbCcc";
    private static final String BOUNDARY = "boundary=";
    private static final String CONTENT_TEMPLATE = "{boundary}\n{xmlPart}\n{boundary}\n{jsonPart}\n{boundary}--";

    public String getMultiPartContent(HttpServletRequest request, String xmlSct, String jsonPart) {
        String contentTypeHeader = request.getHeader(HttpHeaders.CONTENT_TYPE);

        String boundary = DEFAULT_BOUNDARY;
        if (contentTypeHeader != null
                && contentTypeHeader.contains(MediaType.MULTIPART_FORM_DATA_VALUE)
                && contentTypeHeader.contains(BOUNDARY)) {
            String boundaryValue = contentTypeHeader.substring(contentTypeHeader.indexOf(BOUNDARY) + BOUNDARY.length());
            boundary = boundaryValue.startsWith("--") ? boundaryValue : "--" + boundaryValue;
        }
        return CONTENT_TEMPLATE
                   .replaceAll("\\{boundary}", boundary)
                   .replaceAll("\\{xmlPart}", xmlSct)
                   .replaceAll("\\{jsonPart}", jsonPart);
    }
}
