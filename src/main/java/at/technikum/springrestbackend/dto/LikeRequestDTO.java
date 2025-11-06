package at.technikum.springrestbackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeRequestDTO {
    @NotNull(message = "userId darf nicht leer sein")
    private Long userId;

    @NotNull(message = "profileId darf nicht leer sein")
    private Long profileId;
}