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


import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.psd2.consent.api.ConsentType;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.ConsentStatusResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsentModelMapperPsd2 {
    private final Xs2aObjectMapper xs2aObjectMapper;
    private final ScaMethodsMapper scaMethodsMapper;
    private final HrefLinkMapper hrefLinkMapper;

    public Object mapToGetConsentsResponse(Object payment, ConsentType consentType) {
        String rawData;
        if (consentType == ConsentType.AIS) {
            AccountConsent accountConsent = (AccountConsent) payment;
            rawData = convertResponseToRawData(accountConsent.getBody());
            return enrichPaymentWithAdditionalData(rawData, accountConsent);
        }

        return null;
    }

    public ConsentsResponse201 mapToConsentsResponse201(CreateConsentResponse createConsentResponse) {
        return Optional.ofNullable(createConsentResponse)
                   .map(cnst ->
                            new ConsentsResponse201()
                                .consentStatus(ConsentStatus.fromValue(cnst.getConsentStatus()))
                                .consentId(cnst.getConsentId())
                                .scaMethods(scaMethodsMapper.mapToScaMethods(cnst.getScaMethods()))
                                ._links(hrefLinkMapper.mapToLinksMap(cnst.getLinks()))
                                .psuMessage(cnst.getPsuMessage())
                                .tppMessages(mapToTppMessage2XXList(cnst.getTppMessageInformation()))
                   )
                   .orElse(null);
    }

    public ConsentStatusResponse200 mapToConsentStatusResponse200(ConsentStatusResponse consentStatusResponse) {
        return Optional.ofNullable(consentStatusResponse)
                   .map(cstr -> new ConsentStatusResponse200().consentStatus(ConsentStatus.fromValue(cstr.getConsentStatus())))
                   .orElse(null);
    }

    private Object enrichPaymentWithAdditionalData(String rawData, AccountConsent accountConsent) {
        try {
            Map<String, String> map = xs2aObjectMapper.readValue(rawData, Map.class);
            map.put("consentStatus", accountConsent.getConsentStatus().toString());
            return map;
        } catch (JsonProcessingException e) {
            log.warn("Can't convert payment to map {}", e.getMessage());
            return rawData;
        }
    }

    private String convertResponseToRawData(byte[] paymentData) {
        try {
            return IOUtils.toString(paymentData, Charset.defaultCharset().name());
        } catch (IOException e) {
            log.warn("Can not convert payment from byte[] ", e);
            return null;
        }
    }

    private List<TppMessage2XX> mapToTppMessage2XXList(Set<TppMessageInformation> tppMessages) {
        if (CollectionUtils.isEmpty(tppMessages)) {
            return null;
        }
        return tppMessages.stream()
                   .map(this::mapToTppMessage2XX)
                   .collect(Collectors.toList());
    }

    private TppMessage2XX mapToTppMessage2XX(TppMessageInformation tppMessage) {
        TppMessage2XX tppMessage2XX = new TppMessage2XX();
        tppMessage2XX.setCategory(TppMessageCategory.fromValue(tppMessage.getCategory().name()));
        tppMessage2XX.setCode(MessageCode2XX.WARNING);
        tppMessage2XX.setPath(tppMessage.getPath());
        tppMessage2XX.setText(tppMessage.getText());

        return tppMessage2XX;
    }
}
