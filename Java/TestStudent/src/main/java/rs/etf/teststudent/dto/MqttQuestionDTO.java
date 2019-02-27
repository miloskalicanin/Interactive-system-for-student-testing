package rs.etf.teststudent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
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
public class MqttQuestionDTO implements Cloneable {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("id_test")
    private Long idTest;

    @JsonProperty("test_name")
    private String testName;

    @JsonProperty("mqtt_theme_a")
    private String mqttThemeA;

    @JsonProperty("type")
    private TipPitanja type;

    @JsonProperty("question")
    private String question;

    @JsonProperty("answers")
    private List<AnswerDTO> answers;
}
