package rs.etf.teststudent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertRequestDTO {

    //@JsonInclude(value=Include.NON_EMPTY, content=Include.NON_NULL)
    @JsonProperty("email")
    private String email;

    @JsonProperty("csr")
    private byte[] csr;
}
