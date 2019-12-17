/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.spi.domain.psu;

import lombok.Value;

import java.util.UUID;

/**
 * Contains data about PSU known in scope of the request
 */
@Value
public class SpiPsuData {
    private String psuId;
    private String psuIdType;
    private String psuCorporateId;
    private String psuCorporateIdType;
    private String psuIpAddress;
    private String psuIpPort;
    private String psuUserAgent;
    private String psuGeoLocation;
    private String psuAccept;
    private String psuAcceptCharset;
    private String psuAcceptEncoding;
    private String psuAcceptLanguage;
    private String psuHttpMethod;
    private UUID psuDeviceId;

    public static SpiPsuDataBuilder builder() {
        return new SpiPsuDataBuilder();
    }

    public static class SpiPsuDataBuilder {
        private String psuId;
        private String psuIdType;
        private String psuCorporateId;
        private String psuCorporateIdType;
        private String psuIpAddress;
        private String psuIpPort;
        private String psuUserAgent;
        private String psuGeoLocation;
        private String psuAccept;
        private String psuAcceptCharset;
        private String psuAcceptEncoding;
        private String psuAcceptLanguage;
        private String psuHttpMethod;
        private UUID psuDeviceId;

        public SpiPsuDataBuilder psuId(String psuId) {
            this.psuId = psuId;
            return this;
        }

        public SpiPsuDataBuilder psuIdType(String psuIdType) {
            this.psuIdType = psuIdType;
            return this;
        }

        public SpiPsuDataBuilder psuCorporateId(String psuCorporateId) {
            this.psuCorporateId = psuCorporateId;
            return this;
        }

        public SpiPsuDataBuilder psuCorporateIdType(String psuCorporateIdType) {
            this.psuCorporateIdType = psuCorporateIdType;
            return this;
        }

        public SpiPsuDataBuilder psuIpAddress(String psuIpAddress) {
            this.psuIpAddress = psuIpAddress;
            return this;
        }

        public SpiPsuDataBuilder psuIpPort(String psuIpPort) {
            this.psuIpPort = psuIpPort;
            return this;
        }

        public SpiPsuDataBuilder psuUserAgent(String psuUserAgent) {
            this.psuUserAgent = psuUserAgent;
            return this;
        }

        public SpiPsuDataBuilder psuGeoLocation(String psuGeoLocation) {
            this.psuGeoLocation = psuGeoLocation;
            return this;
        }

        public SpiPsuDataBuilder psuAccept(String psuAccept) {
            this.psuAccept = psuAccept;
            return this;
        }

        public SpiPsuDataBuilder psuAcceptCharset(String psuAcceptCharset) {
            this.psuAcceptCharset = psuAcceptCharset;
            return this;
        }

        public SpiPsuDataBuilder psuAcceptEncoding(String psuAcceptEncoding) {
            this.psuAcceptEncoding = psuAcceptEncoding;
            return this;
        }

        public SpiPsuDataBuilder psuAcceptLanguage(String psuAcceptLanguage) {
            this.psuAcceptLanguage = psuAcceptLanguage;
            return this;
        }

        public SpiPsuDataBuilder psuHttpMethod(String psuHttpMethod) {
            this.psuHttpMethod = psuHttpMethod;
            return this;
        }

        public SpiPsuDataBuilder psuDeviceId(UUID psuDeviceId) {
            this.psuDeviceId = psuDeviceId;
            return this;
        }

        public SpiPsuData build() {
            return new SpiPsuData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, psuIpAddress, psuIpPort, psuUserAgent, psuGeoLocation, psuAccept, psuAcceptCharset, psuAcceptEncoding, psuAcceptLanguage, psuHttpMethod, psuDeviceId);
        }
    }
}
