package rs.etf.teststudent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnswerDTO implements Serializable {

    @JsonProperty("id_given_answer")
    private Long idGivenAnswer;

    @JsonProperty("id_question")
    private Long idQuestion;

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("num_answered")
    private Integer numAnswered;

    public AnswerDTO() {
    }

    public AnswerDTO(Long idGivenAnswer, Long idQuestion, String answer, Integer numAnswered) {
        this.idGivenAnswer = idGivenAnswer;
        this.idQuestion = idQuestion;
        this.answer = answer;
        this.numAnswered = numAnswered;
    }

    public Long getIdGivenAnswer() {
        return idGivenAnswer;
    }

    public void setIdGivenAnswer(Long idGivenAnswer) {
        this.idGivenAnswer = idGivenAnswer;
    }

    public Long getIdQuestion() {
        return idQuestion;
    }

    public void setIdQuestion(Long idQuestion) {
        this.idQuestion = idQuestion;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getNumAnswered() {
        return numAnswered;
    }

    public void setNumAnswered(Integer numAnswered) {
        this.numAnswered = numAnswered;
    }

    @Override
    public String toString() {
        return "AnswerDTO{" +
                "idGivenAnswer=" + idGivenAnswer +
                ", idQuestion=" + idQuestion +
                ", answer='" + answer + '\'' +
                ", numAnswered=" + numAnswered +
                '}';
    }
}
