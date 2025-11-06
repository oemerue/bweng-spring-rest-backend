package at.technikum.springrestbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginRequestDTO {

    @NotBlank(message = "Email darf nicht leer sein")
    @Email(message = "Email sollte gültig sein")
    private String email;

    @NotBlank(message = "Password darf nicht leer sein")
    private String password;
}
