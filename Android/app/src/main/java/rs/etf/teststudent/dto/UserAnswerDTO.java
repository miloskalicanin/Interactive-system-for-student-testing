package rs.etf.teststudent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAnswerDTO implements Serializable {

    @JsonProperty("email")
    private String email;

    @JsonProperty("answer")
    private AnswerDTO answer;

    public UserAnswerDTO() {
    }

    public UserAnswerDTO(String email, AnswerDTO answer) {
        this.email = email;
        this.answer = answer;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AnswerDTO getAnswer() {
        return answer;
    }

    public void setAnswer(AnswerDTO answer) {
        this.answer = answer;
    }

    @Override
    public String toString() {
        return "UserAnswerDTO{" +
                "email='" + email + '\'' +
                ", answer=" + answer +
                '}';
    }
}