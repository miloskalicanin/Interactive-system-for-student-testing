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
public class AnswerDTO {

    @JsonProperty("id_given_answer")
    private Long idGivenAnswer;

    @JsonProperty("id_question")
    private Long idQuestion;

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("num_answered")
    private Integer numAnswered;
}
