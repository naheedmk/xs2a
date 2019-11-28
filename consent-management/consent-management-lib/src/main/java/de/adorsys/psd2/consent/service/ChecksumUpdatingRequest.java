package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.ais.AisAccountAccess;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Value
@RequiredArgsConstructor
public class ChecksumUpdatingRequest {
    private AisAccountAccess accountAccess;
    private AisAccountAccess aspspAccountAccess;
    private LocalDate validUntil;
    private int frequencyPerDay;
    private Boolean combinedServiceIndicator;
    private Boolean recurringIndicator;

    public Map<ChecksumType, Object> getAll() {
        Map<ChecksumType, Object> map = new HashMap<>();
        putIfNotNull(map, ChecksumType.ACCOUNT_ACCESSES, accountAccess);
        putIfNotNull(map, ChecksumType.ASPSP_ACCOUNT_ACCESSES, aspspAccountAccess);
        putIfNotNull(map, ChecksumType.VALID_UNTIL, validUntil);
        putIfNotNull(map, ChecksumType.FREQUENCY_PER_DAY, frequencyPerDay);
        putIfNotNull(map, ChecksumType.COMBINED_SERVICE_INDICATOR, combinedServiceIndicator);
        putIfNotNull(map, ChecksumType.RECURRING_INDICATOR, recurringIndicator);
        return map;
    }

    private void putIfNotNull(Map<ChecksumType, Object> map, ChecksumType key, Object value) {
        if (value == null) {
            return;
        }
        map.put(key, value);
    }
}
