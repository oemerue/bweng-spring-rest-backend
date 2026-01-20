package at.technikum.springrestbackend.dto;

import jakarta.validation.constraints.Size;

public class PostUpdateDTO {

    @Size(max = 150, message = "Title must be <= 150 characters")
    private String title;

    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
