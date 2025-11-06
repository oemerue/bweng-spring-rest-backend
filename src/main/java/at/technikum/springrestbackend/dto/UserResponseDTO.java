package at.technikum.springrestbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String email;
    private String username;
    private String country;
    private String profilePictureUrl;
    private String role;  // USER, ADMIN
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
