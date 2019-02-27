package rs.etf.teststudent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CertResponseDTO implements Serializable {

    @JsonProperty("certificate")
    private byte[] certificate;

    public CertResponseDTO() {
    }

    public CertResponseDTO(byte[] certificate) {
        this.certificate = certificate;
    }

    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] certificate) {
        this.certificate = certificate;
    }

    @Override
    public String toString() {
        return "CertResponseDTO{" +
                "certificate=" + Arrays.toString(certificate) +
                '}';
    }
}
