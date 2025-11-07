package at.technikum.springrestbackend.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class UpdateProfileDTO {
    @NotBlank public String username;
    @NotNull @Min(18) @Max(120) public Integer age;
    @NotBlank public String gender;
    @NotBlank public String city;
    @NotBlank public String country;
    @Size(max=500) public String bio;
    @Pattern(regexp="^https?://.+", message="avatarUrl muss eine gültige URL sein")
    public String avatarUrl;
    @NotEmpty public List<@NotBlank String> interests;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }
}
