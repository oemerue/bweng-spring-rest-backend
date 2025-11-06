package at.technikum.springrestbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {

    private Long userId;
    private String username;
    private String email;
    private String token;  // JWT Token
    private String tokenType;  // z.B. "Bearer"
    private Long expiresIn;  // Token Expiry in milliseconds
}
