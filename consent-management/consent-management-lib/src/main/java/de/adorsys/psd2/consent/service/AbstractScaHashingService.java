package de.adorsys.psd2.consent.service;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class AbstractScaHashingService implements HashingService{
    @Override
    public byte[] hash( String data, Charset charset ){
        try{
            return MessageDigest.getInstance(getAlgorithmName())
                .digest(data.getBytes());
        } catch( NoSuchAlgorithmException e){
            throw new IllegalArgumentException("No such hashing algorithm: "  + getAlgorithmName());
        }
    }

    public abstract String getAlgorithmName();
}
