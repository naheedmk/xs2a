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


import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
    uses = {HrefLinkMapper.class, CoreObjectsMapper.class},

    imports = {HrefLinkMapper.class, AuthenticationType.class})
//@DecoratedWith(AuthorisationMapperDecorator.class)
public interface AuthorisationMapper1 {
    static final String HREF = "href";

    @Mapping(target = "authenticationType", expression = "java( AuthenticationType.fromValue(xs2aAuthenticationObject.getAuthenticationType()) )")
//    @Mapping(target = "authenticationType", source = "xs2aAuthenticationObject", qualifiedByName = "mapToAuthenticationType")
    ChosenScaMethod mapToChosenScaMethod(Xs2aAuthenticationObject xs2aAuthenticationObject);

    //    @Mapping(target = "authorisationIds", source = "xs2AAuthorisationSubResources.authorisationIds")
    Authorisations mapToAuthorisations(Xs2aAuthorisationSubResources xs2AAuthorisationSubResources);

    @Mapping(target = "authenticationType", expression = "java( AuthenticationType.fromValue(xs2aAuthenticationObject.getAuthenticationType()) )")
    AuthenticationObject mapToScaMethod(Xs2aAuthenticationObject xs2aAuthenticationObject);


    @IterableMapping(elementTargetType = AuthenticationObject.class, nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    ScaMethods getAvailableScaMethods(List<Xs2aAuthenticationObject> availableScaMethods);

    default String mapToAuthenticationType1(Xs2aAuthenticationObject xs2aAuthenticationObject) {
        if (true) {

        }
        return AuthenticationType.fromValue(xs2aAuthenticationObject.getAuthenticationType()).toString();
    }
}
