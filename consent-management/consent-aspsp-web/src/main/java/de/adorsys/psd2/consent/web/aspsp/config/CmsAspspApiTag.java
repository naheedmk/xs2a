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

package de.adorsys.psd2.consent.web.aspsp.config;

import lombok.Getter;

@Getter
public enum CmsAspspApiTag {
    ASPSP_EXPORT_AIS_CONSENTS("ASPSP Export AIS Consents", "Provides access to the consent management system for exporting AIS consents by ASPSP"),
    ASPSP_EVENTS("ASPSP Events", "Provides access to the consent management system for ASPSP Events"),
    ASPSP_PIIS_CONSENTS("ASPSP PIIS, Consents", "Controller for CMS-ASPSP-API providing access to PIIS consents"),
    ASPSP_PIIS_CONSENTS_EXPORT("ASPSP PIIS Consents Export", "Provides access to the consent management system for exporting PIIS consents by ASPSP"),
    ASPSP_EXPORT_PAYMENTS("ASPSP Export Payments", "Provides access to the consent management system for exporting PIS payments by ASPSP"),
    ASPSP_TPP_STOP_LIST("ASPSP TPP Stop List", "Provides access to the consent management system TPP Stop List"),
    ASPSP_TPP_INFO("ASPSP TPP Info", "Provides access to the consent management system TPP Info");

    private final String tagName;
    private final String tagDescription;

    CmsAspspApiTag(String tagName, String tagDescription) {
        this.tagName = tagName;
        this.tagDescription = tagDescription;
    }
}
