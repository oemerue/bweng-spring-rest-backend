package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.CreateProfileDTO;
import at.technikum.springrestbackend.dto.ProfileResponseDTO;
import at.technikum.springrestbackend.dto.UpdateProfileDTO;
import at.technikum.springrestbackend.entity.Profile;
import at.technikum.springrestbackend.repository.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository repo;

    public ProfileServiceImpl(ProfileRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<ProfileResponseDTO> getAll() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public ProfileResponseDTO getById(Long id) {
        return toDto(findOrThrow(id));
    }

    @Override
    public ProfileResponseDTO create(CreateProfileDTO dto) {
        if (repo.existsByUsername(dto.username)) {
            throw new IllegalArgumentException("username bereits vergeben");
        }

        Profile p = new Profile(
                dto.username,
                dto.age,
                dto.gender,
                dto.city,
                dto.country,
                dto.bio,
                dto.avatarUrl,
                dto.interests
        );

        return toDto(repo.save(p));
    }

    @Override
    public ProfileResponseDTO update(Long id, UpdateProfileDTO dto) {
        Profile p = findOrThrow(id);
        if (!p.getUsername().equals(dto.username) && repo.existsByUsername(dto.username)) {
            throw new IllegalArgumentException("username bereits vergeben");
        }
        apply(p, dto.username, dto.age, dto.gender, dto.city, dto.country, dto.bio, dto.avatarUrl, dto.interests);
        return toDto(repo.save(p));
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new EntityNotFoundException("Profile nicht gefunden");
        repo.deleteById(id);
    }

    // --- helpers ---
    private Profile findOrThrow(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Profile nicht gefunden"));
    }

    private void apply(Profile p, String username, Integer age, String gender, String city,
                       String country, String bio, String avatarUrl, List<String> interests) {
        p.setUsername(username);
        p.setAge(age);
        p.setGender(gender);
        p.setCity(city);
        p.setCountry(country);
        p.setBio(bio);
        p.setAvatarUrl(avatarUrl);
        p.setInterests(interests);
    }

    private ProfileResponseDTO toDto(Profile p) {
        ProfileResponseDTO dto = new ProfileResponseDTO();
        dto.id = p.getId();
        dto.username = p.getUsername();
        dto.age = p.getAge();
        dto.gender = p.getGender();
        dto.city = p.getCity();
        dto.country = p.getCountry();
        dto.bio = p.getBio();
        dto.avatarUrl = p.getAvatarUrl();
        dto.interests = p.getInterests();
        return dto;
    }
}
