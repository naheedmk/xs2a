package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.domain.Checksum;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ChecksumService {
    public static final Charset CHARSET = Charset.defaultCharset();

    private final Sha512HashingService hashingService;

    public boolean updateChecksum(AisConsent consent, ChecksumUpdatingRequest checksumUpdatingRequest) {
        Map<ChecksumType, Object> updateValues = checksumUpdatingRequest.getAll();

        //TODO: #449 return correct boolean
        for (ChecksumType checksumTypeOfUpdateValue : updateValues.keySet()) {
            Object updateValue = updateValues.get(checksumTypeOfUpdateValue);
            updateChecksum(consent, checksumTypeOfUpdateValue, updateValue.toString());
        }

        return true;
    }

    public boolean updateChecksum(AisConsent consent, ChecksumType checksumType, String input) {
        Checksum checksumData = consent.getChecksum();
        ChecksumValue checksumValue = checksumData.getBy(checksumType);

        if (!isUpdatable(consent, checksumValue.getChecksum())) {
            return false;
        }

        checksumValue.setChecksum(calculateChecksum(input));
        return true;
    }

    private byte[] calculateChecksum(String checksumSource) {
        return hashingService.hash(checksumSource, CHARSET);
    }

    private boolean isUpdatable(AisConsent consent, byte[] checksum) {
        ConsentStatus consentStatus = consent.getConsentStatus();

        if (!consentStatus.isFinalisedStatus() && consentStatus != ConsentStatus.VALID) {
            return true;
        }

        if (checksum == null) {
            return true;
        }

        return false;
    }
}
