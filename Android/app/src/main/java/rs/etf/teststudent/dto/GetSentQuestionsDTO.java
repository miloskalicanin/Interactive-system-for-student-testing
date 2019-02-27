package rs.etf.teststudent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetSentQuestionsDTO {

    @JsonProperty("email")
    private String email;

    @JsonProperty("mqtt_themes_q")
    private List<String> mqttThemesQ;

    @JsonProperty("questions")
    private List<MqttQuestionDTO> questions;

    public GetSentQuestionsDTO() {
    }

    public GetSentQuestionsDTO(String email, List<String> mqttThemesQ, List<MqttQuestionDTO> questions) {
        this.email = email;
        this.mqttThemesQ = mqttThemesQ;
        this.questions = questions;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getMqttThemesQ() {
        return mqttThemesQ;
    }

    public void setMqttThemesQ(List<String> mqttThemesQ) {
        this.mqttThemesQ = mqttThemesQ;
    }

    public List<MqttQuestionDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<MqttQuestionDTO> questions) {
        this.questions = questions;
    }

    @Override
    public String toString() {
        return "GetSentQuestionsDTO{" +
                "email='" + email + '\'' +
                ", mqttThemesQ=" + mqttThemesQ +
                ", questions=" + questions +
                '}';
    }
}
