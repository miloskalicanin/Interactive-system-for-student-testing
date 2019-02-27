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
public class TestDTO implements Cloneable{

    @JsonProperty("id")
    private Long id;

    @JsonProperty("course_key")
    private String courseKey;

    @JsonProperty("classroom")
    private String classroom;

    @JsonProperty("test_name")
    private String testName;

    @JsonProperty("date")
    private String date;

    @JsonProperty("finished")
    private boolean finished;

    @JsonProperty("mqtt_theme_q")
    private String mqttThemeQ;

    @JsonProperty("mqtt_theme_a")
    private String mqttThemeA;

    @Override
    public TestDTO clone() throws CloneNotSupportedException {
        return (TestDTO) super.clone();
    }
}
