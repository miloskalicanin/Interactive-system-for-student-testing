package rs.etf.teststudent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoDTO implements Serializable {

    @JsonProperty("type")
    private String type;

    @JsonProperty("name")
    private String name;

    @JsonProperty("last_name")
    private String lastNname;

    @JsonProperty("email")
    private String email;

    public UserInfoDTO() {
    }

    public UserInfoDTO(String type, String name, String lastNname, String email) {
        this.type = type;
        this.name = name;
        this.lastNname = lastNname;
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastNname() {
        return lastNname;
    }

    public void setLastNname(String lastNname) {
        this.lastNname = lastNname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "UserInfoDTO{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", lastNname='" + lastNname + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
