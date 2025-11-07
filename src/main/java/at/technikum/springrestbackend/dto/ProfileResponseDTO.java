package at.technikum.springrestbackend.dto;

import java.util.List;

public class ProfileResponseDTO {
    public Long id;
    public String username;
    public Integer age;
    public String gender;
    public String city;
    public String country;
    public String bio;
    public String avatarUrl;
    public List<String> interests;
}
