package de.adorsys.psd2.consent.service;

import lombok.Data;

import javax.persistence.*;

@Data
@Embeddable
public class ChecksumValue {
    @Column
    @Enumerated(EnumType.STRING)
    public final ChecksumType checksumType;
    @Lob
    @Column
    private byte[] checksum;

    public ChecksumValue(){
        this(null);
    }

    public ChecksumValue(ChecksumType checksumType) {
        this.checksumType = checksumType;
    }
}
