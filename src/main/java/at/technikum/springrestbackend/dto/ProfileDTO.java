package at.technikum.springrestbackend.dto;

import java.time.LocalDateTime;

public class ProfileDTO {
    private Long id;
    private String username;
    private String email;
    private String country;
    private String bio;
    private Integer age;
    private String city;
    private String gender;
    private String avatarUrl; // public URL
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProfileDTO(Long id, String username, String email, String country, String bio,
                      Integer age, String city, String gender, String avatarUrl, String role,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.country = country;
        this.bio = bio;
        this.age = age;
        this.city = city;
        this.gender = gender;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getCountry() {
        return country;
    }

    public String getBio() {
        return bio;
    }

    public Integer getAge() {
        return age;
    }

    public String getCity() {
        return city;
    }

    public String getGender() {
        return gender;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
