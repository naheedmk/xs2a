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

package de.adorsys.psd2.xs2a.web.validator.path;

import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Class with common functionality (AIS and PIS) for path parameters validating.
 */
public class AbstractPathParameterValidatorImpl {

    protected ErrorBuildingService errorBuildingService;
    protected AspspProfileServiceWrapper aspspProfileServiceWrapper;

    protected AbstractPathParameterValidatorImpl(ErrorBuildingService errorBuildingService, AspspProfileServiceWrapper aspspProfileServiceWrapper) {
        this.errorBuildingService = errorBuildingService;
        this.aspspProfileServiceWrapper = aspspProfileServiceWrapper;
    }

    protected Map<String, String> getPathVariableMap(HttpServletRequest request) {
        //noinspection unchecked
        return (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    }
}
