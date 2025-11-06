package at.technikum.springrestbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {

    private Long id;

    @NotNull(message = "Receiver ID darf nicht null sein")
    private Long receiverId;  // An wen sende ich die Message

    @NotBlank(message = "Message content darf nicht leer sein")
    private String content;

    private Long senderId;
    private String senderUsername;
    private String receiverUsername;

    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
