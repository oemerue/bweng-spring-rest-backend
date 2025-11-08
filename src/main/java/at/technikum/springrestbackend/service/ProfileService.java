package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.CreateProfileDTO;
import at.technikum.springrestbackend.dto.ProfileResponseDTO;
import at.technikum.springrestbackend.dto.UpdateProfileDTO;

import java.util.List;

public interface ProfileService {
    List<ProfileResponseDTO> getAll();
    ProfileResponseDTO getById(Long id);
    ProfileResponseDTO create(CreateProfileDTO dto);
    ProfileResponseDTO update(Long id, UpdateProfileDTO dto);
    void delete(Long id);
}
