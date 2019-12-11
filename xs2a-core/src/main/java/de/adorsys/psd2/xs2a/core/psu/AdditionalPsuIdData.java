package de.adorsys.psd2.xs2a.core.psu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class AdditionalPsuIdData {
    private String psuIpAddress;
    private String psuIpPort;
    private String psuUserAgent;
    private String psuGeoLocation;
    private String psuAccept;
    private String psuAcceptCharset;
    private String psuAcceptEncoding;
    private String psuAcceptLanguage;
    private String psuHttpMethod;
    private String psuDeviceId;

    public AdditionalPsuIdData(String psuIpAddress, String psuIpPort, String psuUserAgent, String psuGeoLocation, String psuAccept, String psuAcceptCharset,
                               String psuAcceptEncoding, String psuAcceptLanguage, String psuHttpMethod, UUID psuDeviceId) {
        this.psuIpAddress = psuIpAddress;
        this.psuIpPort = psuIpPort;
        this.psuUserAgent = psuUserAgent;
        this.psuGeoLocation = psuGeoLocation;
        this.psuAccept = psuAccept;
        this.psuAcceptCharset = psuAcceptCharset;
        this.psuAcceptEncoding = psuAcceptEncoding;
        this.psuAcceptLanguage = psuAcceptLanguage;
        this.psuHttpMethod = psuHttpMethod;
        this.psuDeviceId = psuDeviceId == null ? null : psuDeviceId.toString();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return psuIpAddress == null && psuIpPort == null && psuUserAgent == null && psuGeoLocation == null && psuAccept == null
                   && psuAcceptCharset == null && psuAcceptEncoding == null && psuAcceptLanguage == null && psuHttpMethod == null && psuDeviceId == null;
    }

}
