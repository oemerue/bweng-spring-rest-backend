package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.dto.ProfileDTO;
import at.technikum.springrestbackend.dto.ProfileUpdateDTO;
import at.technikum.springrestbackend.dto.PublicProfileDTO;
import at.technikum.springrestbackend.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value = "/api/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<List<PublicProfileDTO>> getAllProfiles() {
        return ResponseEntity.ok(profileService.getAllProfilesPublic());
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<PublicProfileDTO> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getProfilePublic(id));
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileDTO> getMyProfile() {
        return ResponseEntity.ok(profileService.getMyProfile());
    }

    @PutMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfileDTO> updateMyProfile(@Valid @RequestBody ProfileUpdateDTO dto) {
        return ResponseEntity.ok(profileService.updateMyProfile(dto));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileDTO> uploadAvatar(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(profileService.uploadAvatar(file));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyProfile() {
        profileService.deleteMyProfile();
        return ResponseEntity.noContent().build();
    }
}
