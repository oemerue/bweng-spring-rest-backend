package at.technikum.springrestbackend.dto;

import java.time.LocalDateTime;

public class PostDTO {
    private Long id;
    private Long authorId;
    private String authorUsername;
    private String title;
    private String content;
    private String imageUrl; // public URL
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostDTO(Long id, Long authorId, String authorUsername,
                   String title, String content, String imageUrl,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
