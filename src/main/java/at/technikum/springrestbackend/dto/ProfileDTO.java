package at.technikum.springrestbackend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDTO {

    private Long id;
    private Long userId;
    private String username;
    private String profilePictureUrl;

    @NotNull(message = "Alter darf nicht leer sein")
    @Min(value = 18, message = "Alter muss mindestens 18 Jahre sein")
    @Max(value = 120, message = "Alter kann nicht mehr als 120 Jahre sein")
    private Integer age;

    @NotBlank(message = "Location darf nicht leer sein")
    private String location;

    @NotBlank(message = "About darf nicht leer sein")
    private String about;

    private Set<String> interests;  // ["Art", "Yoga", "Music", etc.]

    private Long profileViews;
    private Long likesReceived;
    private Double responseRate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
