package at.technikum.springrestbackend.dto;

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
    private Integer age;
    private String location;
    private String about;
    private Set<String> interests;
    private Long likesReceived;
    private Long profileViews;
    private Double responseRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
