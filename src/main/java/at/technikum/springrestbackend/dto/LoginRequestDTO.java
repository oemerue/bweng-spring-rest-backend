package at.technikum.springrestbackend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequestDTO {

    @NotBlank(message = "Identifier is required")
    @Size(max = 255, message = "Identifier is too long")
    @JsonAlias({"identifier", "username"})
    private String email;

    @NotBlank(message = "Password is required")
    @Size(max = 255, message = "Password is too long")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdentifier() {
        return email;
    }

    public void setIdentifier(String identifier) {
        this.email = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
