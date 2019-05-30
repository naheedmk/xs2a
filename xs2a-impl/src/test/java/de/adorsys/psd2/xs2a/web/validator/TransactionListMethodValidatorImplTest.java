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

package de.adorsys.psd2.xs2a.web.validator;

import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.body.BodyValidator;
import de.adorsys.psd2.xs2a.web.validator.header.account.TransactionListHeaderValidator;
import de.adorsys.psd2.xs2a.web.validator.path.account.TransactionListPathValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TransactionListMethodValidatorImplTest {
    private static final String METHOD_NAME = "_getTransactionList";
    private static final String PATH_PARAMETER_NAME = "some parameter name";
    private static final String PATH_PARAMETER_VALUE = "some parameter value";
    private static final String ANOTHER_PATH_PARAMETER_VALUE = "some another value";

    private TransactionListMethodValidatorImpl transactionListMethodValidator;
    @Mock
    private TransactionListHeaderValidator transactionListHeaderValidator;
    @Mock
    private TransactionListPathValidator transactionListPathValidator;
    @Mock
    private MessageError messageError;
    private MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();

    @Before
    public void setUp() {
        transactionListMethodValidator = new TransactionListMethodValidatorImpl(Collections.singletonList(transactionListHeaderValidator),
                                                                                Collections.singletonList(transactionListPathValidator));
    }

    @Test
    public void validate_shouldPassPathParametersToValidators() {
        // Given
        mockHttpServletRequest.addParameter(PATH_PARAMETER_NAME, PATH_PARAMETER_VALUE);

        Map<String, List<String>> expectedParams = new HashMap<>();
        expectedParams.put(PATH_PARAMETER_NAME, Collections.singletonList(PATH_PARAMETER_VALUE));

        // noinspection unchecked
        ArgumentCaptor<Map<String, List<String>>> pathParamCaptor = ArgumentCaptor.forClass(Map.class);


        // When
        transactionListMethodValidator.validate(mockHttpServletRequest, messageError);

        // Then
        verify(transactionListPathValidator).validate(pathParamCaptor.capture(), eq(messageError));
        assertEquals(expectedParams, pathParamCaptor.getValue());
    }

    @Test
    public void validate_withNoPathParamsInRequest_shouldPassEmptyMap() {
        // Given
        // noinspection unchecked
        ArgumentCaptor<Map<String, List<String>>> pathParamCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        transactionListMethodValidator.validate(mockHttpServletRequest, messageError);

        // Then
        verify(transactionListPathValidator).validate(pathParamCaptor.capture(), eq(messageError));
        assertTrue(pathParamCaptor.getValue().isEmpty());
    }

    @Test
    public void validate_withMultipleValuesForOneParam_shouldPassValuesInList() {
        // Given
        mockHttpServletRequest.addParameter(PATH_PARAMETER_NAME, PATH_PARAMETER_VALUE);
        mockHttpServletRequest.addParameter(PATH_PARAMETER_NAME, ANOTHER_PATH_PARAMETER_VALUE);

        Map<String, List<String>> expectedParams = new HashMap<>();
        expectedParams.put(PATH_PARAMETER_NAME, Arrays.asList(PATH_PARAMETER_VALUE, ANOTHER_PATH_PARAMETER_VALUE));

        // noinspection unchecked
        ArgumentCaptor<Map<String, List<String>>> pathParamCaptor = ArgumentCaptor.forClass(Map.class);


        // When
        transactionListMethodValidator.validate(mockHttpServletRequest, messageError);

        // Then
        verify(transactionListPathValidator).validate(pathParamCaptor.capture(), eq(messageError));
        assertEquals(expectedParams, pathParamCaptor.getValue());
    }

    @Test
    public void getValidators_shouldReturnValidatorsFromConstructors() {
        // When
        List<TransactionListPathValidator> actualPathValidators = transactionListMethodValidator.getPathParameterValidators();
        List<TransactionListHeaderValidator> actualHeaderValidators = transactionListMethodValidator.getHeaderValidators();
        List<BodyValidator> actualBodyValidators = transactionListMethodValidator.getBodyValidators();

        // Then
        assertEquals(Collections.singletonList(transactionListHeaderValidator), actualHeaderValidators);
        assertEquals(Collections.singletonList(transactionListPathValidator), actualPathValidators);
        assertTrue(actualBodyValidators.isEmpty());
    }

    @Test
    public void getMethodName_shouldReturnCorrectName() {
        // When
        String actualName = transactionListMethodValidator.getMethodName();

        // Then
        assertEquals(METHOD_NAME, actualName);
    }
}
