package at.technikum.springrestbackend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterRequestDTO {

    @NotBlank(message = "Email darf nicht leer sein")
    @Email(message = "Email sollte gültig sein")
    private String email;

    @NotBlank(message = "Username darf nicht leer sein")
    @Size(min = 5, message = "Username muss mindestens 5 Zeichen lang sein")
    private String username;

    @NotBlank(message = "Password darf nicht leer sein")
    @Size(min = 8, message = "Password muss mindestens 8 Zeichen lang sein, mit Groß-, Kleinbuchstaben und Zahlen")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$", 
             message = "Password muss Großbuchstaben, Kleinbuchstaben und Zahlen enthalten")
    private String password;

    @NotBlank(message = "Country code darf nicht leer sein")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country code sollte 2 Großbuchstaben sein (z.B. AT, DE)")
    private String country;
}
