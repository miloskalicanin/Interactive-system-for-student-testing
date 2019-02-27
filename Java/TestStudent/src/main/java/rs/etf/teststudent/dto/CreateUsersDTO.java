package rs.etf.teststudent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUsersDTO {

    @JsonProperty("email")
    //@JsonInclude(value=Include.NON_EMPTY, content=Include.NON_NULL)
    private String email;

    @JsonProperty("file_data")
    private String fileData;
}
