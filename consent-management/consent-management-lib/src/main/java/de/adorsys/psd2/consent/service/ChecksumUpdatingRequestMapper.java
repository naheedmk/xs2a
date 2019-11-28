package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentAccessRequest;
import org.springframework.stereotype.Component;

@Component
public class ChecksumUpdatingRequestMapper {
    public ChecksumUpdatingRequest toChecksumUpdatingRequest(CmsAisConsentAccessRequest cmsAisConsentAccessRequest) {
        return new ChecksumUpdatingRequest(
            null,
            cmsAisConsentAccessRequest.getAccountAccess(),
            cmsAisConsentAccessRequest.getValidUntil(),
            cmsAisConsentAccessRequest.getFrequencyPerDay(),
            cmsAisConsentAccessRequest.getCombinedServiceIndicator(),
            cmsAisConsentAccessRequest.getRecurringIndicator()
        );
    }
}
