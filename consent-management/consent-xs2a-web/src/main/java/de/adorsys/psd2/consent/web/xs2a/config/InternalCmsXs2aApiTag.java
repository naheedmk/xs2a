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

package de.adorsys.psd2.consent.web.xs2a.config;

import lombok.Getter;

@Getter
public enum InternalCmsXs2aApiTag {
    AIS_CONSENTS("AIS, Consents", "Provides access to consent management system for AIS"),
    AIS_PSU_DATA("AIS, PSU Data", "Provides access to consent management system for PSU Data"),
    ASPSP_CONSENT_DATA("Aspsp Consent Data", "Provides access to consent management system for AspspDataConsent"),
    EVENTS("Events", "Provides access to the consent management system for Events"),
    PIIS_CONSENTS("PIIS, Consents", "Provides access to consent management system for PIIS"),
    PIS_COMMON_PAYMENT("PIS, Common Payment", "Provides access to common payment system for PIS"),
    PIS_PAYMENTS("PIS, Payments", "Provides access to consent management system for PIS"),
    PIS_PSU_DATA("PIS, PSU Data", "Provides access to consent management system for PSU Data"),
    TPP("TPP", "Provides access to the TPP");

    private final String tagName;
    private final String tagDescription;

    InternalCmsXs2aApiTag(String tagName, String tagDescription) {
        this.tagName = tagName;
        this.tagDescription = tagDescription;
    }
}
