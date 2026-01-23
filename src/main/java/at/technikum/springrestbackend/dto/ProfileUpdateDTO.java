package at.technikum.springrestbackend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProfileUpdateDTO {

    @Size(min = 5, max = 50, message = "Username must be between 5 and 50 characters")
    private String username;

    @Size(max = 500, message = "Bio must be at most 500 characters")
    private String bio;

    @Min(value = 0, message = "Age must be >= 0")
    @Max(value = 120, message = "Age must be <= 120")
    private Integer age;

    @Size(max = 120, message = "City must be at most 120 characters")
    private String city;

    @Pattern(regexp = "(?i)MALE|FEMALE|OTHER",
            message = "Gender must be MALE, FEMALE, or OTHER")
    private String gender;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
