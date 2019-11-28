package de.adorsys.psd2.consent.domain;

import de.adorsys.psd2.consent.service.ChecksumType;
import de.adorsys.psd2.consent.service.ChecksumValue;

import javax.persistence.*;
import java.util.*;

@Embeddable

public class Checksum {
    @ElementCollection
    @CollectionTable(name = "ais_consent_checksum")
    @JoinColumn(name = "checksum_id", referencedColumnName = "id")
    private Set<ChecksumValue> values;

    @Transient
    public Map<ChecksumType, ChecksumValue> container;

    public Checksum() {
        this.values = new HashSet<>();
        Collections.addAll(values, new ChecksumValue(ChecksumType.ACCOUNT_ACCESSES),
                           new ChecksumValue(ChecksumType.ASPSP_ACCOUNT_ACCESSES),
                           new ChecksumValue(ChecksumType.RECURRING_INDICATOR),
                           new ChecksumValue(ChecksumType.VALID_UNTIL),
                           new ChecksumValue(ChecksumType.FREQUENCY_PER_DAY),
                           new ChecksumValue(ChecksumType.COMBINED_SERVICE_INDICATOR));

        this.container = new HashMap<>();
        for (ChecksumValue value : values) {
            container.put(value.getChecksumType(), value);
        }
    }

    public ChecksumValue getBy(ChecksumType checksumType) {
        return container.get(checksumType);
    }
}
