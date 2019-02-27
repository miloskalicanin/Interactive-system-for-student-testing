package rs.etf.teststudent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionDTO implements Serializable {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("id_test")
    private Long idTest;

    @JsonProperty("type")
    private TipPitanja type;

    @JsonProperty("question")
    private String question;

    @JsonProperty("answers")
    private List<AnswerDTO> answers;

    public QuestionDTO() {
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
        return "QuestionDTO{" +
                "id=" + id +
                ", idTest=" + idTest +
                ", type=" + type +
                ", question='" + question + '\'' +
                ", answers=" + answers +
                '}';
    }
}