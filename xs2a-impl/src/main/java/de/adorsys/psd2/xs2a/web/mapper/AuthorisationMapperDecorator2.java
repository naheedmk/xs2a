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

import de.adorsys.psd2.api.ConsentApi;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.model.StartScaprocessResponse;
import de.adorsys.psd2.model.UpdatePsuAuthenticationResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.REDIRECT;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public abstract class AuthorisationMapperDecorator2 extends AuthorisationMapper2 {

    @Autowired
    private HrefLinkMapper hrefLinkMapper;
    @Autowired
    private ScaApproachResolver scaApproachResolver;
    @Autowired
    private AuthorisationMapper2 delegate;
    @Autowired
    private RedirectLinkBuilder redirectLinkBuilder;
    @Autowired
    private AspspProfileService aspspProfileService;

    @Override
    public StartScaprocessResponse mapToPisCreateOrUpdateAuthorisationResponse(Xs2aCreatePisAuthorisationResponse response){
        StartScaprocessResponse scaprocessResponse = delegate.mapToPisCreateOrUpdateAuthorisationResponse(response);
        scaprocessResponse.setLinks(hrefLinkMapper.mapToLinksMap(response.getLinks()));
        return scaprocessResponse;
    }

    @Override
    public UpdatePsuAuthenticationResponse mapToPisCreateOrUpdateAuthorisationResponse(Xs2aUpdatePisCommonPaymentPsuDataResponse response){
        UpdatePsuAuthenticationResponse updatePsuAuthenticationResponse = delegate.mapToPisCreateOrUpdateAuthorisationResponse(response);
        updatePsuAuthenticationResponse.setLinks(hrefLinkMapper.mapToLinksMap(response.getLinks()));
        return updatePsuAuthenticationResponse;
    }

    @Override
    public UpdatePsuAuthenticationResponse mapToAisCreateOrUpdateAuthorisationResponse(UpdateConsentPsuDataResponse response){
        UpdatePsuAuthenticationResponse updatePsuAuthenticationResponse = delegate.mapToAisCreateOrUpdateAuthorisationResponse(response);
        updatePsuAuthenticationResponse.setLinks(hrefLinkMapper.mapToLinksMap(response.getLinks()));
        return updatePsuAuthenticationResponse;
    }

    @Override
    public StartScaprocessResponse mapToAisCreateOrUpdateAuthorisationResponse(CreateConsentAuthorizationResponse response){
        StartScaprocessResponse scaprocessResponse = delegate.mapToAisCreateOrUpdateAuthorisationResponse(response);

        String link = scaApproachResolver.resolveScaApproach() == REDIRECT
            ? redirectLinkBuilder.buildConsentScaRedirectLink(response.getConsentId(), response.getAuthorisationId())
            : createUpdateConsentsPsuDataLink(response);
        scaprocessResponse.setLinks(hrefLinkMapper.mapToLinksMap(response.getResponseLinkType().getValue(), link));
        return scaprocessResponse;
    }

    private String createUpdateConsentsPsuDataLink(CreateConsentAuthorizationResponse csar) {
        URI uri = linkTo(methodOn(ConsentApi.class)._updateConsentsPsuData(null, csar.getConsentId(), csar.getAuthorisationId(), null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null)).toUri();

        UriComponentsBuilder uriComponentsBuilder = aspspProfileService.getAspspSettings().isForceXs2aBaseUrl()
            ? UriComponentsBuilder.fromHttpUrl(aspspProfileService.getAspspSettings().getXs2aBaseUrl()).path(uri.getPath())
            : UriComponentsBuilder.fromUri(uri);
        return uriComponentsBuilder.toUriString();
    }

}
