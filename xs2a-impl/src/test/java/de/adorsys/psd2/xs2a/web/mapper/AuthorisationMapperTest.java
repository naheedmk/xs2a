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

import de.adorsys.psd2.model.AuthenticationObject;
import de.adorsys.psd2.model.Authorisations;
import de.adorsys.psd2.model.ChosenScaMethod;
import de.adorsys.psd2.model.ScaMethods;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AuthorisationMapper1Impl.class, AuthorisationMapper2Impl_.class})
public class AuthorisationMapperTest {

    @Autowired
    private AuthorisationMapper1 mapper;
    @Autowired
    private AuthorisationMapper2 mapper2;

    private AuthorisationMapper oldMapper;
    private JsonReader jsonReader = new JsonReader();

    @Before
    public void setUp() throws Exception {
        oldMapper = new AuthorisationMapper(null, null, null, null, null, null);
    }

    @Test
    public void mapToChosenScaMethod() {

        Xs2aAuthenticationObject authenticationObject = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-Xs2aAuthenticationObject-initial.json", Xs2aAuthenticationObject.class);
        ChosenScaMethod actualChosenScaMethod = mapper2.mapToChosenScaMethod(authenticationObject);

        ChosenScaMethod expectedChosenScaMethod = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-ChosenScaMethod.json", ChosenScaMethod.class);

        assertEquals(expectedChosenScaMethod.getAuthenticationMethodId(), actualChosenScaMethod.getAuthenticationMethodId());
        assertEquals(expectedChosenScaMethod.getAuthenticationType(), actualChosenScaMethod.getAuthenticationType());
        assertEquals(expectedChosenScaMethod.getName(), actualChosenScaMethod.getName());
        assertEquals(expectedChosenScaMethod.getExplanation(), actualChosenScaMethod.getExplanation());
        assertEquals(expectedChosenScaMethod.getAuthenticationVersion(), actualChosenScaMethod.getAuthenticationVersion());
    }

    @Test
    public void mapToAuthorisations_equals_success() {
        Xs2aAuthorisationSubResources xs2AAuthorisationSubResources = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-Xs2aAutorisationSubResources.json", Xs2aAuthorisationSubResources.class);
        Authorisations actualAuthorisations = mapper2.mapToAuthorisations(xs2AAuthorisationSubResources);

        Authorisations expectedAuthorisations = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-Authorisations.json", Authorisations.class);

        for (int i = 0; i < actualAuthorisations.getAuthorisationIds().size(); i++) {
            assertEquals(expectedAuthorisations.getAuthorisationIds().get(i), actualAuthorisations.getAuthorisationIds().get(i));
        }
    }

    @Test
    public void getAvailableScaMethods() {
        List<Xs2aAuthenticationObject> availableScaMethods = jsonReader.getListFromFile("json/service/mapper/AuthorisationMapper-Xs2aAuthenticationObjects-List.json", Xs2aAuthenticationObject.class);
        ScaMethods actualScaMethods = mapper2.getAvailableScaMethods(availableScaMethods);

        ScaMethods expectedScaMethods = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-ScaMethods.json", ScaMethods.class);

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
