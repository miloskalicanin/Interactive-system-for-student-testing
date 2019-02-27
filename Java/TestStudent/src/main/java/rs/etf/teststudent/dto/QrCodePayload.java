/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class QrCodePayload {

    @JsonProperty("course")
    private String course;

    @JsonProperty("classroom")
    private String classroom;

    @JsonProperty("test_name")
    private String testName;

    @JsonProperty("date")
    private String date;

    @JsonProperty("mqtt_theme_q")
    private String mqttThemeQ;
}
