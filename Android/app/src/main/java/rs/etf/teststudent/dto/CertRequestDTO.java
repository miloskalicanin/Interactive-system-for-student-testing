package rs.etf.teststudent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CertRequestDTO implements Serializable {

    //@JsonInclude(value=Include.NON_EMPTY, content=Include.NON_NULL)
    @JsonProperty("email")
    private String email;

    @JsonProperty("csr")
    private byte[] csr;

    public CertRequestDTO() {
    }

    public CertRequestDTO(String email, byte[] csr) {
        this.email = email;
        this.csr = csr;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getCsr() {
        return csr;
    }

    public void setCsr(byte[] csr) {
        this.csr = csr;
    }

    @Override
    public String toString() {
        return "CertRequestDTO{" +
                "email='" + email + '\'' +
                ", csr=" + Arrays.toString(csr) +
                '}';
    }
}
