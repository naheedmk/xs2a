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

package de.adorsys.psd2.consent.web.psu.controller;

import de.adorsys.psd2.consent.psu.api.CmsPsuConfirmationOfFundsService;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.mapper.config.ObjectMapperConfig;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CmsPsuConfirmationOfFundsControllerTest {
    private static final String CONSENT_ID = "6b94ecdd-fd44-4ffb-bd56-d41ee433a792";
    private static final String AUTHORISATION_ID = "70baa209-6a2f-4cc1-8834-d8a819bf5e9f";
    private static final String INSTANCE_ID = "instance id";
    private static final String PSU_ID = "psu id";
    private static final String PSU_ID_TYPE = "psu id type";
    private static final String PSU_CORPORATE_ID = "psu corporate id";
    private static final String PSU_CORPORATE_ID_TYPE = "psu corporate id type";
    private static final String SCA_STATUS_RECEIVED = "RECEIVED";
    private static final String NOK_REDIRECT_URI = "https://everything_is_bad.html";

    private static final HttpHeaders PSU_HEADERS = buildPsuHeaders();
    private static final HttpHeaders INSTANCE_ID_HEADERS = buildInstanceIdHeaders();
    private static final String INSTANCE_ID_HEADER_NAME = "instance-id";
    private static final String PSU_ID_HEADER_NAME = "psu-id";
    private static final String PSU_ID_TYPE_HEADER_NAME = "psu-id-type";
    private static final String PSU_CORPORATE_ID_HEADER_NAME = "psu-corporate-id";
    private static final String PSU_CORPORATE_ID_TYPE_HEADER_NAME = "psu-corporate-id-type";
    private static final byte[] EMPTY_BODY = new byte[0];

    private MockMvc mockMvc;
    private JsonReader jsonReader = new JsonReader();

    @Mock
    private CmsPsuConfirmationOfFundsService cmsPsuConfirmationOfFundsService;

    @InjectMocks
    private CmsPsuConfirmationOfFunds cmsPsuConfirmationOfFundsController;

    private PsuIdData psuIdData;
    private AuthenticationDataHolder authenticationDataHolder;

    @BeforeEach
    void setUp() {
        Xs2aObjectMapper xs2aObjectMapper = new ObjectMapperConfig().xs2aObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(cmsPsuConfirmationOfFundsController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(xs2aObjectMapper))
                      .build();
        psuIdData = jsonReader.getObjectFromFile("json/psu-id-data.json", PsuIdData.class);
        authenticationDataHolder = jsonReader.getObjectFromFile("json/authentication-data-holder.json", AuthenticationDataHolder.class);
    }

    @Test
    void updateAuthorisationStatus_withValidRequest_shouldReturnOk() throws Exception {
        // Given
        when(cmsPsuConfirmationOfFundsService.updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder))
            .thenReturn(true);
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/authentication-data-holder.json");

        // When
        mockMvc.perform(put("/psu-api/v2/piis/consent/{consent-id}/authorisation/{authorisation-id}/status/{status}", CONSENT_ID, AUTHORISATION_ID, SCA_STATUS_RECEIVED)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isOk())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuConfirmationOfFundsService).updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder);
    }

    @Test
    void updateAuthorisationStatus_withFalseServiceResponse_shouldReturnBadRequest() throws Exception {
        // Given
        when(cmsPsuConfirmationOfFundsService.updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder))
            .thenReturn(false);
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/authentication-data-holder.json");

        // When
        mockMvc.perform(put("/psu-api/v2/piis/consent/{consent-id}/authorisation/{authorisation-id}/status/{status}", CONSENT_ID, AUTHORISATION_ID, SCA_STATUS_RECEIVED)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuConfirmationOfFundsService).updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder);
    }

    @Test
    void updateAuthorisationStatus_withInvalidScaStatus_shouldReturnBadRequest() throws Exception {
        // Given
        String invalidScaStatus = "invalid SCA status";
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/authentication-data-holder.json");

        // When
        mockMvc.perform(put("/psu-api/v2/piis/consent/{consent-id}/authorisation/{authorisation-id}/status/{status}", CONSENT_ID, AUTHORISATION_ID, invalidScaStatus)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuConfirmationOfFundsService, never()).updateAuthorisationStatus(any(), anyString(), anyString(), any(), anyString(), any());
    }

    @Test
    void updateAuthorisationStatus_onExpiredAuthorisationException_shouldReturnNokLink() throws Exception {
        // Given
        when(cmsPsuConfirmationOfFundsService.updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder))
            .thenThrow(new AuthorisationIsExpiredException(NOK_REDIRECT_URI));
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/authentication-data-holder.json");
        String timeoutResponse = jsonReader.getStringFromFile("json/ais/response/ais-consent-timeout.json");

        // When
        mockMvc.perform(put("/psu-api/v2/piis/consent/{consent-id}/authorisation/{authorisation-id}/status/{status}", CONSENT_ID, AUTHORISATION_ID, SCA_STATUS_RECEIVED)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().json(timeoutResponse));

        // Then
        verify(cmsPsuConfirmationOfFundsService).updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder);
    }

    private static HttpHeaders buildInstanceIdHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(INSTANCE_ID_HEADER_NAME, INSTANCE_ID);
        return httpHeaders;
    }

    private static HttpHeaders buildPsuHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(PSU_ID_HEADER_NAME, PSU_ID);
        httpHeaders.add(PSU_ID_TYPE_HEADER_NAME, PSU_ID_TYPE);
        httpHeaders.add(PSU_CORPORATE_ID_HEADER_NAME, PSU_CORPORATE_ID);
        httpHeaders.add(PSU_CORPORATE_ID_TYPE_HEADER_NAME, PSU_CORPORATE_ID_TYPE);
        return httpHeaders;
    }
}
