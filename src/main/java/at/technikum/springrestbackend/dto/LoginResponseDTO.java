package at.technikum.springrestbackend.dto;

public class LoginResponseDTO {

    public String token;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token) {
        this.token = token;
    }
}
