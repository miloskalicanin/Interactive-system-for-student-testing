package rs.etf.teststudent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MqttQuestionDTO implements Serializable {

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

    public MqttQuestionDTO() {
    }

    public MqttQuestionDTO(Long id, Long idTest, String testName, String mqttThemeA, TipPitanja type, String question, List<AnswerDTO> answers) {
        this.id = id;
        this.idTest = idTest;
        this.testName = testName;
        this.mqttThemeA = mqttThemeA;
        this.type = type;
        this.question = question;
        this.answers = answers;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdTest() {
        return idTest;
    }

    public void setIdTest(Long idTest) {
        this.idTest = idTest;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getMqttThemeA() {
        return mqttThemeA;
    }

    public void setMqttThemeA(String mqttThemeA) {
        this.mqttThemeA = mqttThemeA;
    }

    public TipPitanja getType() {
        return type;
    }

    public void setType(TipPitanja type) {
        this.type = type;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<AnswerDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerDTO> answers) {
        this.answers = answers;
    }

    @Override
    public String toString() {
        return "MqttQuestionDTO{" +
                "id=" + id +
                ", idTest=" + idTest +
                ", testName='" + testName + '\'' +
                ", mqttThemeA='" + mqttThemeA + '\'' +
                ", type=" + type +
                ", question='" + question + '\'' +
                ", answers=" + answers +
                '}';
    }

    public QuestionDTO toQuestionDTO() {
        QuestionDTO questionDTO = new QuestionDTO();

        questionDTO.setId(id);
        questionDTO.setIdTest(idTest);
        questionDTO.setType(type);
        questionDTO.setQuestion(question);
        questionDTO.setAnswers(answers);

        return questionDTO;
    }
}
