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

package de.adorsys.psd2.xs2a.web.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AuthorisationMapper1Impl.class})
public class AuthorisationMapperTest {

    private static final String HREF = "href";

    @Autowired
    private AuthorisationMapper1 mapper;

    private AuthorisationMapper2 mapper2 = new AuthorisationMapper2Impl_();

    private AuthorisationMapper oldMapper;
    private JsonReader jsonReader = new JsonReader();

    @Before
    public void setUp() throws Exception {
        HrefLinkMapper hrefMapper = new HrefLinkMapper(new ObjectMapper());
        oldMapper = new AuthorisationMapper(new CoreObjectsMapper(), null, null, null, hrefMapper, null);
    }

    @Test
    public void mapToAuthorisations_equals_success() {
        // given
        Authorisations expectedAuthorisations = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-Authorisations.json", Authorisations.class);
        Xs2aAuthorisationSubResources xs2AAuthorisationSubResources = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-Xs2aAutorisationSubResources.json", Xs2aAuthorisationSubResources.class);

        // when
        Authorisations actualAuthorisations = mapper2.mapToAuthorisations(xs2AAuthorisationSubResources);

        // then
        for (int i = 0; i < actualAuthorisations.getAuthorisationIds().size(); i++) {
            assertEquals(expectedAuthorisations.getAuthorisationIds().get(i), actualAuthorisations.getAuthorisationIds().get(i));
        }
    }

    @Test
    public void mapToPisCreateOrUpdateAuthorisationResponse_for_Xs2aCreatePisAuthorisationResponse() {
        // given
        StartScaprocessResponse expectedStartScaProcessResponse = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-StartScaProcessResponse-expected.json", StartScaprocessResponse.class);

        Xs2aCreatePisAuthorisationResponse xs2aCreatePisAuthorisationResponse = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-StartScaProcessResponse-ResponseObject.json", Xs2aCreatePisAuthorisationResponse.class);
        ResponseObject<Xs2aCreatePisAuthorisationResponse> responseObject = ResponseObject.<Xs2aCreatePisAuthorisationResponse>builder()
                                                                                .body(xs2aCreatePisAuthorisationResponse)
                                                                                .build();

        // when
        StartScaprocessResponse actualStartScaprocessResponse = (StartScaprocessResponse) oldMapper.mapToPisCreateOrUpdateAuthorisationResponse(responseObject);

        // then
        assertEquals(expectedStartScaProcessResponse.getScaStatus(), actualStartScaprocessResponse.getScaStatus());
        assertEquals(expectedStartScaProcessResponse.getAuthorisationId(), actualStartScaprocessResponse.getAuthorisationId());
        assertThatLinksMappedCorrect(actualStartScaprocessResponse.getLinks());
    }

    @Test
    public void mapToPisCreateOrUpdateAuthorisationResponse_for_Xs2aUpdatePisCommonPaymentPsuDataResponse() {
        // given
        UpdatePsuAuthenticationResponse expectedUpdatePsuAuthenticationResponse = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-UpdatePsuAuthenticationResponse-expected.json", UpdatePsuAuthenticationResponse.class);

        Xs2aUpdatePisCommonPaymentPsuDataResponse xs2aUpdatePisCommonPaymentPsuDataResponse = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-UpdatePsuAuthenticationResponse-ResponseObject.json", Xs2aUpdatePisCommonPaymentPsuDataResponse.class);
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> responseObject = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                                                                                       .body(xs2aUpdatePisCommonPaymentPsuDataResponse)
                                                                                       .build();
        // when
        UpdatePsuAuthenticationResponse actualUpdatePsuAuthenticationResponse = (UpdatePsuAuthenticationResponse) oldMapper.mapToPisCreateOrUpdateAuthorisationResponse(responseObject);

        // then
        assertThatLinksMappedCorrect(actualUpdatePsuAuthenticationResponse.getLinks());
        assertThatAvailableScaMethodsEquals(expectedUpdatePsuAuthenticationResponse.getScaMethods(), actualUpdatePsuAuthenticationResponse.getScaMethods());
        assertThatChosenScaMethodsEquals(expectedUpdatePsuAuthenticationResponse.getChosenScaMethod(), actualUpdatePsuAuthenticationResponse.getChosenScaMethod());
        assertEquals(expectedUpdatePsuAuthenticationResponse.getPsuMessage(), actualUpdatePsuAuthenticationResponse.getPsuMessage());
        assertEquals(expectedUpdatePsuAuthenticationResponse.getChallengeData(), actualUpdatePsuAuthenticationResponse.getChallengeData());
        assertEquals(expectedUpdatePsuAuthenticationResponse.getScaStatus(), actualUpdatePsuAuthenticationResponse.getScaStatus());
    }

    @Test
    public void mapToChosenScaMethod() {
        // given
        ChosenScaMethod expectedChosenScaMethod = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-ChosenScaMethod.json", ChosenScaMethod.class);
        Xs2aAuthenticationObject authenticationObject = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-Xs2aAuthenticationObject-initial.json", Xs2aAuthenticationObject.class);

        // when
        ChosenScaMethod actualChosenScaMethod = mapper2.mapToChosenScaMethod(authenticationObject);

        // then
        assertThatChosenScaMethodsEquals(expectedChosenScaMethod, actualChosenScaMethod);
    }

    @Test
    public void getAvailableScaMethods() {
        // given
        List<Xs2aAuthenticationObject> availableScaMethods = jsonReader.getListFromFile("json/service/mapper/AuthorisationMapper-Xs2aAuthenticationObjects-List.json", Xs2aAuthenticationObject.class);
        ScaMethods actualScaMethods = mapper2.getAvailableScaMethods(availableScaMethods);

        // when
        ScaMethods expectedScaMethods = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-ScaMethods.json", ScaMethods.class);

        // then
        assertThatAvailableScaMethodsEquals(expectedScaMethods, actualScaMethods);
    }

    private void assertThatLinksMappedCorrect(Map actualLinks) {
        assertNotNull(actualLinks);
        assertNotNull(actualLinks.get("scaRedirect"));
        assertEquals("", ((Map) actualLinks.get("self")).get(HREF));
        assertNull(((Map) actualLinks.get("scaOAuth")).get(HREF));
    }

    private void assertThatChosenScaMethodsEquals(ChosenScaMethod expectedChosenScaMethod, ChosenScaMethod actualChosenScaMethod) {
        assertEquals(expectedChosenScaMethod.getAuthenticationMethodId(), actualChosenScaMethod.getAuthenticationMethodId());
        assertEquals(expectedChosenScaMethod.getAuthenticationType(), actualChosenScaMethod.getAuthenticationType());
        assertEquals(expectedChosenScaMethod.getName(), actualChosenScaMethod.getName());
        assertEquals(expectedChosenScaMethod.getExplanation(), actualChosenScaMethod.getExplanation());
        assertEquals(expectedChosenScaMethod.getAuthenticationVersion(), actualChosenScaMethod.getAuthenticationVersion());
    }

    private void assertThatAvailableScaMethodsEquals(ScaMethods expectedScaMethods, ScaMethods actualScaMethods) {
        for (int i = 0; i < actualScaMethods.size(); i++) {
            AuthenticationObject actualAuthenticationObject = actualScaMethods.get(i);
            AuthenticationObject expectedAuthenticationObject = expectedScaMethods.get(i);

            assertEquals(expectedAuthenticationObject.getAuthenticationMethodId(), actualAuthenticationObject.getAuthenticationMethodId());
            assertEquals(expectedAuthenticationObject.getAuthenticationType(), actualAuthenticationObject.getAuthenticationType());
            assertEquals(expectedAuthenticationObject.getName(), actualAuthenticationObject.getName());
            assertEquals(expectedAuthenticationObject.getExplanation(), actualAuthenticationObject.getExplanation());
            assertEquals(expectedAuthenticationObject.getAuthenticationVersion(), actualAuthenticationObject.getAuthenticationVersion());
        }
    }
}
