package de.adorsys.psd2.consent.service;

import java.nio.charset.Charset;

public interface HashingService {
    byte[] hash(String data, Charset charset);
}
