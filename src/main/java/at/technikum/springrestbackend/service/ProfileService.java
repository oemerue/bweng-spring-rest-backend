package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.CreateProfileDTO;
import at.technikum.springrestbackend.dto.ProfileDTO;

import java.util.List;

public interface ProfileService {
    ProfileDTO create(CreateProfileDTO dto);
    ProfileDTO update(Long userId, CreateProfileDTO dto);
    ProfileDTO getByUserId(Long userId);

    List<ProfileDTO> getAllProfiles(int page, int size);
    List<ProfileDTO> getProfilesByAgeRange(Integer minAge, Integer maxAge, int page, int size);
    List<ProfileDTO> getProfilesByLocation(String location, int page, int size);
    List<ProfileDTO> getMostLikedProfiles(int page, int size);
    List<ProfileDTO> getMostViewedProfiles(int page, int size);
}
