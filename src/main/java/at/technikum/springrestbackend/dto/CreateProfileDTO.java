package at.technikum.springrestbackend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProfileDTO {

    @NotNull(message = "Alter darf nicht leer sein")
    @Min(value = 18, message = "Alter muss mindestens 18 Jahre sein")
    @Max(value = 120, message = "Alter kann nicht mehr als 120 Jahre sein")
    private Integer age;

    @NotBlank(message = "Location darf nicht leer sein")
    private String location;

    @NotBlank(message = "About darf nicht leer sein")
    @Size(min = 10, max = 500, message = "About muss zwischen 10 und 500 Zeichen lang sein")
    private String about;

    @NotEmpty(message = "Mindestens 1 Interest erforderlich")
    private Set<Long> interestIds;  // IDs der Interessen [1, 2, 3, etc.]

    private String profilePictureUrl;  // Optional
}
