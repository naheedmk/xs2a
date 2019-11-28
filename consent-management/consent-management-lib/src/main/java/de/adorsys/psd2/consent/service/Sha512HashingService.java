package de.adorsys.psd2.consent.service;

import org.springframework.stereotype.Service;

@Service
public class Sha512HashingService extends AbstractScaHashingService {
    @Override
    public String getAlgorithmName() {
        return "SHA-512";
    }
}
