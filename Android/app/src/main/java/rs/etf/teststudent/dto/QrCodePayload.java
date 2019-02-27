/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.teststudent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QrCodePayload implements Serializable {

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

    public QrCodePayload() {
    }

    public QrCodePayload(String course, String classroom, String testName, String date, String mqttThemeQ) {
        this.course = course;
        this.classroom = classroom;
        this.testName = testName;
        this.date = date;
        this.mqttThemeQ = mqttThemeQ;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMqttThemeQ() {
        return mqttThemeQ;
    }

    public void setMqttThemeQ(String mqttThemeQ) {
        this.mqttThemeQ = mqttThemeQ;
    }

    @Override
    public String toString() {
        return "QrCodePayload{" +
                "course='" + course + '\'' +
                ", classroom='" + classroom + '\'' +
                ", testName='" + testName + '\'' +
                ", date='" + date + '\'' +
                ", mqttThemeQ='" + mqttThemeQ + '\'' +
                '}';
    }
}
