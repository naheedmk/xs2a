package de.adorsys.psd2.consent.config;

import org.springframework.beans.factory.annotation.Value;

public class AisConsentRemoteUrls {
    @Value("${consent-service.baseurl:http://localhost:38080/api/v1}")
    private String consentServiceBaseUrl;

    /**
     * @return VOID
     * Method: POST
     * PathVariables: AisConsentActionRequest consentActionRequest
     */
    public String consentActionLog() {
        return consentServiceBaseUrl + "/ais/consent/action";
    }
}
