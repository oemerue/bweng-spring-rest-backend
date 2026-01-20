package at.technikum.springrestbackend.dto;

import java.time.LocalDateTime;

public class PublicProfileDTO {

    private Long id;
    private String username;
    private String bio;
    private Integer age;
    private String city;
    private String country;
    private String gender;
    private String avatarUrl;
    private LocalDateTime createdAt;

    public PublicProfileDTO(Long id, String username, String bio, Integer age,
                            String city, String country, String gender,
                            String avatarUrl, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.bio = bio;
        this.age = age;
        this.city = city;
        this.country = country;
        this.gender = gender;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
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

    public String getCountry() {
        return country;
    }

    public String getGender() {
        return gender;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}