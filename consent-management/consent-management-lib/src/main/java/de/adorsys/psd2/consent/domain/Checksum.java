package de.adorsys.psd2.consent.domain;

import de.adorsys.psd2.consent.service.ChecksumType;
import de.adorsys.psd2.consent.service.ChecksumValue;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Embeddable
public class Checksum {
    @ElementCollection
    @CollectionTable(name = "ais_consent_checksum")
    @JoinColumn(name = "checksum_id", referencedColumnName = "id")
    private Set<ChecksumValue> values;

    public Checksum() {
        this.values = new HashSet<>();
        Collections.addAll(values, new ChecksumValue(ChecksumType.ACCOUNT_ACCESSES),
                           new ChecksumValue(ChecksumType.ASPSP_ACCOUNT_ACCESSES),
                           new ChecksumValue(ChecksumType.RECURRING_INDICATOR),
                           new ChecksumValue(ChecksumType.VALID_UNTIL),
                           new ChecksumValue(ChecksumType.FREQUENCY_PER_DAY),
                           new ChecksumValue(ChecksumType.COMBINED_SERVICE_INDICATOR));
    }

    public ChecksumValue getBy(ChecksumType checksumType) {
        for (ChecksumValue value : values) {
            if (value.checksumType == checksumType) {
                return value;
            }
        }
        return null;
    }
}
