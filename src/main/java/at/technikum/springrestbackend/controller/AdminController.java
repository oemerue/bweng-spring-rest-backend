package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.dto.*;
import at.technikum.springrestbackend.service.PostService;
import at.technikum.springrestbackend.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/api/admin", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ProfileService profileService;
    private final PostService postService;

    public AdminController(ProfileService profileService, PostService postService) {
        this.profileService = profileService;
        this.postService = postService;
    }

    @GetMapping("/users")
    public ResponseEntity<Page<ProfileDTO>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(profileService.getAllProfilesForAdmin(pageable));
    }

    @GetMapping("/users/{id:\\d+}")
    public ResponseEntity<ProfileDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getProfileForAdmin(id));
    }

    @PutMapping(value = "/users/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfileDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody ProfileUpdateDTO dto
    ) {
        return ResponseEntity.ok(profileService.updateProfileAsAdmin(id, dto));
    }

    @PutMapping(value = "/users/{id:\\d+}/role", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfileDTO> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateDTO dto
    ) {
        return ResponseEntity.ok(profileService.changeRole(id, dto.getRole()));
    }

    @PutMapping(value = "/users/{id:\\d+}/enabled", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfileDTO> setEnabled(
            @PathVariable Long id,
            @Valid @RequestBody EnabledUpdateDTO dto
    ) {
        return ResponseEntity.ok(profileService.setEnabledAsAdmin(id, dto.getEnabled()));
    }

    @PostMapping(value = "/users/{id:\\d+}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileDTO> uploadUserAvatar(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(profileService.uploadAvatarAsAdmin(id, file));
    }

    @DeleteMapping("/users/{id:\\d+}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        profileService.deleteProfileAsAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<PostDTO>> getAllPosts(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(postService.getAllForAdmin(pageable));
    }

    @DeleteMapping("/posts/{id:\\d+}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deleteAsAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
