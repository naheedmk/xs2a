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

import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Common validator implementation for verifying path parameters
 */
@RequiredArgsConstructor
public abstract class AbstractPathParameterValidatorImpl implements PathParameterValidator {
    private static final String ERROR_TEXT_ABSENT_PARAMETER = "'%s' is missing in request";
    private static final String ERROR_TEXT_BLANK_HEADER = "'%s' should not be blank";
    private static final String ERROR_TEXT_INVALID_VALUE = "'%s' has invalid value";
    protected final ErrorBuildingService errorBuildingService;


    /**
     * Validates the presence of mandatory path parameter by checking whether:
     * <ul>
     * <li>the parameter is present in the request</li>
     * <li>the parameter's value is contained only once in the request</li>
     * <li>the parameter's value is not blank</li>
     * </ul>
     *
     * @param pathParameterMap path parameter map, with parameter names acting as keys
     * @return valid result if the parameter is present only once and doesn't have blank value,
     * validation error otherwise
     */
    protected ValidationResult validateMandatoryParameterPresence(Map<String, List<String>> pathParameterMap) {
        List<String> pathParameterValues = getPathParameterValues(pathParameterMap);
        String pathParameterName = getPathParameterName();

        if (pathParameterValues.isEmpty()) {
            return ValidationResult.invalid(errorBuildingService.buildErrorType(), TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, String.format(ERROR_TEXT_ABSENT_PARAMETER, pathParameterName)));
        }

        if (hasMultipleValues(pathParameterValues)) {
            return ValidationResult.invalid(errorBuildingService.buildErrorType(), TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, String.format(ERROR_TEXT_INVALID_VALUE, pathParameterName)));
        }

        String pathParameterValue = getFirstPathParameterValue(pathParameterMap);
        if (StringUtils.isBlank(pathParameterValue)) {
            return ValidationResult.invalid(errorBuildingService.buildErrorType(), TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, String.format(ERROR_TEXT_BLANK_HEADER, pathParameterName)));
        }

        return ValidationResult.valid();
    }

    /**
     * Returns the name of the path parameter
     *
     * @return path parameter name
     */
    protected abstract String getPathParameterName();

    /**
     * Returns the first value of the path parameter via name from {@link #getPathParameterName()}
     *
     * @param pathParameterMap path parameters from the request
     * @return value of the first path parameter if it was found, <code>null</code> otherwise
     */
    protected String getFirstPathParameterValue(Map<String, List<String>> pathParameterMap) {
        List<String> pathParameterValues = getPathParameterValues(pathParameterMap);

        return pathParameterValues.stream()
                   .findFirst()
                   .orElse(null);
    }

    private List<String> getPathParameterValues(Map<String, List<String>> pathParameterMap) {
        List<String> valuesList = pathParameterMap.get(getPathParameterName());
        return Optional.ofNullable(valuesList)
                   .orElseGet(ArrayList::new);
    }

    private boolean hasMultipleValues(List<String> pathParameterValues) {
        return pathParameterValues.size() > 1;
    }
}
