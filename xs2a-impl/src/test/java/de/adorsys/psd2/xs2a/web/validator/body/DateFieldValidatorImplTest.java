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

package de.adorsys.psd2.xs2a.web.validator.body;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.web.converter.LocalDateConverter;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.raw.FieldExtractor;
import de.adorsys.psd2.xs2a.web.validator.constants.Xs2aRequestBodyDateField;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DateFieldValidatorImplTest {
    private static final String DAY_OF_EXECUTION_FIELD_NAME = "dayOfExecution";
    private static final String REQUESTED_EXECUTION_DATE_FIELD_NAME = "requestedExecutionDate";
    private static final String REQUESTED_EXECUTION_TIME_FIELD_NAME = "requestedExecutionTime";
    private static final String CORRECT_FORMAT_DATE = "2021-10-10";
    private static final String CORRECT_FORMAT_TIME = "2019-01-01T12:00:00+01:00";
    private static final String WRONG_FORMAT_DATE = "07/01/2019 00:00:00";
    private static final String WRONG_FORMAT_TIME = "07/01/2019 00:00:00";
    private static final String CORRECT_DAY_OF_MONTH = "6";
    private static final String WRONG_DAY_OF_MONTH = "666";

    private static final MessageError DAY_OF_EXECUTION_WRONG_VALUE_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_INVALID_DAY_OF_EXECUTION));
    private static final MessageError REQUESTED_EXECUTION_DATE_WRONG_VALUE_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_DATE_FIELD, "requestedExecutionDate", "ISO_DATE", "YYYY-MM-DD"));
    private static final MessageError REQUESTED_EXECUTION_TIME_WRONG_VALUE_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_DATE_FIELD, "requestedExecutionTime", "ISO_DATE_TIME", "YYYY-MM-DD'T'HH:mm:ssZ"));

    private DateFieldValidator validator;
    private MessageError messageError;

    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;

    @BeforeEach
    void setUp() {
        messageError = new MessageError(ErrorType.PIS_400);
        ErrorBuildingService errorService = new ErrorBuildingServiceMock(ErrorType.PIS_400);
        FieldExtractor fieldExtractor = new FieldExtractor(errorService, xs2aObjectMapper);
        validator = new DateFieldValidator(errorService, new LocalDateConverter(), fieldExtractor);
    }

    @Test
    void validate_requestedExecutionDateWrongValue_wrongFormat_error() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(REQUESTED_EXECUTION_DATE_FIELD_NAME), any(TypeReference.class))).thenReturn(Optional.of(WRONG_FORMAT_DATE));

        // When
        validator.validateDateFormat(mockRequest, Collections.singleton(Xs2aRequestBodyDateField.REQUESTED_EXECUTION_DATE), messageError);

        // Then
        assertEquals(REQUESTED_EXECUTION_DATE_WRONG_VALUE_ERROR, messageError);
    }

    @Test
    void validate_requestedExecutionDateWrongValue_success() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(REQUESTED_EXECUTION_DATE_FIELD_NAME), any(TypeReference.class)))
            .thenReturn(Optional.of(CORRECT_FORMAT_DATE));


        // When
        validator.validateDateFormat(mockRequest, Collections.singleton(Xs2aRequestBodyDateField.REQUESTED_EXECUTION_DATE), messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_requestedExecutionTimeWrongValue_wrongFormat_error() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(REQUESTED_EXECUTION_TIME_FIELD_NAME), any(TypeReference.class)))
            .thenReturn(Optional.of(WRONG_FORMAT_TIME));

        // When
        validator.validateDateFormat(mockRequest, Collections.singleton(Xs2aRequestBodyDateField.REQUESTED_EXECUTION_TIME), messageError);

        // Then
        assertEquals(REQUESTED_EXECUTION_TIME_WRONG_VALUE_ERROR, messageError);
    }

    @Test
    void validate_requestedExecutionTimeWrongValue_success() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(REQUESTED_EXECUTION_TIME_FIELD_NAME), any(TypeReference.class))).thenReturn(Optional.of(CORRECT_FORMAT_TIME));

        // When
        validator.validateDateFormat(mockRequest, Collections.singleton(Xs2aRequestBodyDateField.REQUESTED_EXECUTION_TIME), messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_dayOfExecutionWrongValue_wrongFormat_error() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(DAY_OF_EXECUTION_FIELD_NAME), any(TypeReference.class))).thenReturn(Optional.of(WRONG_DAY_OF_MONTH));

        // When
        validator.validateDayOfExecution(mockRequest, messageError);

        // Then
        assertEquals(DAY_OF_EXECUTION_WRONG_VALUE_ERROR, messageError);
    }

    @Test
    void validate_dayOfExecutionWrongValue_success() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(DAY_OF_EXECUTION_FIELD_NAME), any(TypeReference.class))).thenReturn(Optional.of(CORRECT_DAY_OF_MONTH));

        // When
        validator.validateDayOfExecution(mockRequest, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }
}
