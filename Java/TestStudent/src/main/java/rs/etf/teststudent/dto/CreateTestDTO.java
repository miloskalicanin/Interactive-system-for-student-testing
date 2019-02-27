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
public class CreateTestDTO {

    @JsonProperty("email")
    //@JsonInclude(value=Include.NON_EMPTY, content=Include.NON_NULL)
    private String email;

    @JsonProperty("course_key")
    private String courseKey;

    @JsonProperty("classroom")
    private String classroom;

    @JsonProperty("test_name")
    private String testName;

    @JsonProperty("date")
    private String date;
}
