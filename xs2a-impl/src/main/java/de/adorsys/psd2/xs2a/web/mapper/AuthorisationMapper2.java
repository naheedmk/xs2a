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
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper(componentModel = "spring",
    imports = AuthenticationType.class)
@DecoratedWith(AuthorisationMapperDecorator2.class)
public abstract class AuthorisationMapper2 {

    @Autowired
    private CoreObjectsMapper coreObjectsMapper;

    abstract Authorisations mapToAuthorisations(Xs2aAuthorisationSubResources xs2AAuthorisationSubResources);

    @Mapping(target = "links", ignore = true) // mapper for this field in decorator
    abstract StartScaprocessResponse mapToPisCreateOrUpdateAuthorisationResponse(Xs2aCreatePisAuthorisationResponse response);

    @Mapping(target = "links", ignore = true) // mapper for this field in decorator
    @Mapping(target= "scaMethods", source = "availableScaMethods")
    abstract UpdatePsuAuthenticationResponse mapToPisCreateOrUpdateAuthorisationResponse(Xs2aUpdatePisCommonPaymentPsuDataResponse response);

    @Mapping(target = "links", ignore = true) // mapper for this field in decorator
    abstract StartScaprocessResponse mapToAisCreateOrUpdateAuthorisationResponse(CreateConsentAuthorizationResponse createResponse);

    @Mapping(target = "links", ignore = true) // mapper for this field in decorator
    @Mapping(target= "scaMethods", source = "availableScaMethods")
    abstract UpdatePsuAuthenticationResponse mapToAisCreateOrUpdateAuthorisationResponse(UpdateConsentPsuDataResponse response);

    public @NotNull ScaStatusResponse mapToScaStatusResponse(@NotNull ScaStatus scaStatus) {
        return new ScaStatusResponse().scaStatus(coreObjectsMapper.mapToModelScaStatus(scaStatus));
    }

     @Mapping(target = "authenticationType", expression = "java( AuthenticationType.fromValue(xs2aAuthenticationObject.getAuthenticationType()) )")
     abstract ChosenScaMethod mapToChosenScaMethod(Xs2aAuthenticationObject xs2aAuthenticationObject);

    @Mapping(target = "authenticationType", expression = "java( AuthenticationType.fromValue(xs2aAuthenticationObject.getAuthenticationType()) )")
    abstract AuthenticationObject mapToScaMethod(Xs2aAuthenticationObject xs2aAuthenticationObject);

    @IterableMapping(elementTargetType = AuthenticationObject.class, nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    abstract ScaMethods getAvailableScaMethods(List<Xs2aAuthenticationObject> availableScaMethods);

    public Xs2aCreatePisAuthorisationRequest mapToXs2aCreatePisAuthorisationRequest(PsuIdData psuData, String paymentId, String paymentService, String paymentProduct, Map body) {
        return new Xs2aCreatePisAuthorisationRequest(
            paymentId,
            psuData,
            paymentProduct,
            paymentService,
            mapToPasswordFromBody(body));
    }

    private String mapToPasswordFromBody(Map body) {
        return Optional.ofNullable(body)
            .filter(bdy -> !bdy.isEmpty())
            .map(bdy -> bdy.get("psuData"))
            .map(o -> (LinkedHashMap<String, String>) o)
            .map(psuDataMap -> psuDataMap.get("password"))
            .orElse(null);
    }
}
