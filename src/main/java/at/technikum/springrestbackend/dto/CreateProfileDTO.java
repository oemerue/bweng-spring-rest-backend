package at.technikum.springrestbackend.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class CreateProfileDTO {
    @NotBlank public String username;
    @NotNull @Min(18) @Max(120) public Integer age;
    @NotBlank public String gender;
    @NotBlank public String city;
    @NotBlank public String country;
    @Size(max=500) public String bio;
    @Pattern(regexp="^https?://.+", message="avatarUrl muss eine gültige URL sein")
    public String avatarUrl;
    @NotEmpty public List<@NotBlank String> interests;
}
