package rs.etf.teststudent.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetCoursesDTO {

    @JsonProperty("email")
    private String email;

    @JsonProperty("subjects")
    private List<String> subjects;

    @JsonProperty("courses")
    private List<String> courses;
}
