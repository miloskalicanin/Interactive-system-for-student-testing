package rs.etf.teststudent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassCheckInDTO implements Serializable {

    @JsonProperty("mqtt_theme_q")
    private String mqttThemeQ;

    @JsonProperty("email")
    private String email;

    public ClassCheckInDTO() {
    }

    public ClassCheckInDTO(String mqttThemeQ, String email) {
        this.mqttThemeQ = mqttThemeQ;
        this.email = email;
    }

    public String getMqttThemeQ() {
        return mqttThemeQ;
    }

    public void setMqttThemeQ(String mqttThemeQ) {
        this.mqttThemeQ = mqttThemeQ;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "ClassCheckInDTO{" +
                "mqttThemeQ='" + mqttThemeQ + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}