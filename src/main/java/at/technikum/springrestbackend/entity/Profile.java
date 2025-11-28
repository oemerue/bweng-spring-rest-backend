package at.technikum.springrestbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "username darf nicht leer sein")
    @Column(nullable = false, unique = true)
    private String username;

    @NotNull
    @Min(18)
    @Max(120)
    @Column(nullable = false)
    private Integer age;

    @NotBlank(message = "gender darf nicht leer sein")
    private String gender; // "male","female","diverse" etc.

    @NotBlank
    private String city;

    @NotBlank
    private String country;

    @Size(max = 500)
    private String bio;

    @Pattern(regexp = "^https?://.+", message = "avatarUrl muss eine gültige URL sein")
    private String avatarUrl;

    @ElementCollection
    @CollectionTable(name = "profile_interests", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "interest")
    @NotEmpty(message = "mindestens ein Interest erforderlich")
    private List<@NotBlank String> interests = new ArrayList<>();

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role = "ROLE_USER";

    // --- Konstruktoren ---

    // JPA braucht einen parameterlosen Konstruktor
    public Profile() {
    }

    public Profile(
            Long id,
            String username,
            Integer age,
            String gender,
            String city,
            String country,
            String bio,
            String avatarUrl,
            List<@NotBlank String> interests,
            String passwordHash,
            String role) {

        this.id = id;
        this.username = username;
        this.age = age;
        this.gender = gender;
        this.city = city;
        this.country = country;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.interests = interests;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
