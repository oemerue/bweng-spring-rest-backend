package at.technikum.springrestbackend.dto;

public class LoginRequestDTO {

    public String username;
    public String password;

    // Leerer Konstruktor für Jackson
    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
