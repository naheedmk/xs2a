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

import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class AbstractPathParameterValidatorImplTest {

    private AbstractPathParameterValidatorImpl validator;

    @Before
    public void setUp() {
        validator = new AbstractPathParameterValidatorImpl(new ErrorBuildingServiceMock(ErrorType.PIS_400), null);
    }

    @Test
    public void getPathVariableMap_success() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        HashMap<String, String> variables = new HashMap<String, String>() {{
            put("key", "value");
        }};
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, variables);

        // When
        Map actual = validator.getPathVariableMap(mockRequest);


        // Then
        assertEquals(actual, variables);
    }
}
