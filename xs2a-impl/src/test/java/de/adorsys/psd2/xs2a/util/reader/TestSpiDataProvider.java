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

package de.adorsys.psd2.xs2a.util.reader;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;

import java.util.UUID;

public class TestSpiDataProvider {

    private static final UUID X_REQUEST_ID = UUID.randomUUID();
    private static final UUID INTERNAL_REQUEST_ID = UUID.randomUUID();
    private static final String AUTHORISATION = "Bearer 1111111";
    private static final String PSU_ID = "psuId";
    private static final String PSU_ID_TYPE = "psuIdType";
    private static final String PSU_CORPORATE_ID = "psuCorporateId";
    private static final String PSU_CORPORATE_ID_TYPE = "psuCorporateIdType";
    private static final String PSU_IP_ADDRESS = "psuIpAddress";
    private static final String PSU_IP_PORT = "psuIpPort";
    private static final String PSU_USER_AGENT = "psuUserAgent";
    private static final String PSU_GEO_LOCATION = "psuGeoLocation";
    private static final String PSU_ACCEPT = "psuAccept";
    private static final String PSU_ACCEPT_CHARSET = "psuAcceptCharset";
    private static final String PSU_ACCEPT_ENCODING = "psuAcceptEncoding";
    private static final String PSU_ACCEPT_LANGUAGE = "psuAcceptLanguage";
    private static final String PSU_HTTP_METHOD = "psuHttpMethod";
    private static final UUID PSU_DEVICE_ID = UUID.randomUUID();

    public static SpiContextData getSpiContextData() {
        return new SpiContextData(
            new SpiPsuData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS, PSU_IP_PORT, PSU_USER_AGENT, PSU_GEO_LOCATION, PSU_ACCEPT, PSU_ACCEPT_CHARSET, PSU_ACCEPT_ENCODING, PSU_ACCEPT_LANGUAGE, PSU_HTTP_METHOD, PSU_DEVICE_ID),
            new TppInfo(),
            X_REQUEST_ID,
            INTERNAL_REQUEST_ID,
            AUTHORISATION
        );
    }

}
