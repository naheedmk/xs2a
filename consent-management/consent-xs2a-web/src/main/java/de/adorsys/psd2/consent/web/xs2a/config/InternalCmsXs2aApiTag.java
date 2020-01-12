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
    AIS_CONSENTS(InternalCmsXs2aApiTagName.AIS_CONSENTS, "Provides access to consent management system for AIS"),
    AIS_PSU_DATA(InternalCmsXs2aApiTagName.AIS_PSU_DATA, "Provides access to consent management system for PSU Data"),
    ASPSP_CONSENT_DATA(InternalCmsXs2aApiTagName.ASPSP_CONSENT_DATA, "Provides access to consent management system for AspspDataConsent"),
    EVENTS(InternalCmsXs2aApiTagName.EVENTS, "Provides access to the consent management system for Events"),
    PIIS_CONSENTS(InternalCmsXs2aApiTagName.PIIS_CONSENTS, "Provides access to consent management system for PIIS"),
    PIS_COMMON_PAYMENT(InternalCmsXs2aApiTagName.PIS_COMMON_PAYMENT, "Provides access to common payment system for PIS"),
    PIS_PAYMENTS(InternalCmsXs2aApiTagName.PIS_PAYMENTS, "Provides access to consent management system for PIS"),
    PIS_PSU_DATA(InternalCmsXs2aApiTagName.PIS_PSU_DATA, "Provides access to consent management system for PSU Data"),
    TPP(InternalCmsXs2aApiTagName.TPP, "Provides access to the TPP");

    private final String tagName;
    private final String tagDescription;

    InternalCmsXs2aApiTag(String tagName, String tagDescription) {
        this.tagName = tagName;
        this.tagDescription = tagDescription;
    }
}
