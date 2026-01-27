package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.ProfileDTO;
import at.technikum.springrestbackend.dto.ProfileUpdateDTO;
import at.technikum.springrestbackend.dto.PublicProfileDTO;
import at.technikum.springrestbackend.entity.Profile;
import at.technikum.springrestbackend.entity.Role;
import at.technikum.springrestbackend.repository.PostRepository;
import at.technikum.springrestbackend.repository.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private ProfileService profileService;

    private Profile createTestProfile(Long id, Role role) {
        Profile profile = new Profile();
        profile.setId(id);
        profile.setEmail("test" + id + "@example.com");
        profile.setUsernameDisplay("User" + id);
        profile.setCountry("AT");
        profile.setRole(role);
        profile.setPasswordHash("hash");
        return profile;
    }

    private void authenticateAs(Profile profile) {
        var auth = new UsernamePasswordAuthenticationToken(profile, null, profile.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    // PUBLIC ENDPOINTS

    @Test
    void getAllProfilesPublic_returnsListOfPublicProfiles() {
        Profile p1 = createTestProfile(1L, Role.USER);
        Profile p2 = createTestProfile(2L, Role.USER);

        when(profileRepository.findAll()).thenReturn(List.of(p1, p2));

        List<PublicProfileDTO> result = profileService.getAllProfilesPublic();

        assertEquals(2, result.size());
        assertEquals("User1", result.get(0).getUsername());
        assertEquals("User2", result.get(1).getUsername());
    }

    @Test
    void getAllProfilesPublic_emptyList_returnsEmpty() {
        when(profileRepository.findAll()).thenReturn(Collections.emptyList());

        List<PublicProfileDTO> result = profileService.getAllProfilesPublic();

        assertTrue(result.isEmpty());
    }

    @Test
    void getProfilePublic_existingId_returnsProfile() {
        Profile profile = createTestProfile(1L, Role.USER);
        profile.setBio("Test bio");
        profile.setCity("Vienna");

        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));

        PublicProfileDTO result = profileService.getProfilePublic(1L);

        assertEquals(1L, result.getId());
        assertEquals("User1", result.getUsername());
        assertEquals("Test bio", result.getBio());
        assertEquals("Vienna", result.getCity());
        assertEquals("AT", result.getCountry());
    }

    @Test
    void getProfilePublic_nonExistingId_throws404() {
        when(profileRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> profileService.getProfilePublic(999L));
    }

    // ==================== AUTHENTICATED ENDPOINTS ====================

    @Test
    void getMyProfile_authenticated_returnsFullProfile() {
        Profile me = createTestProfile(1L, Role.USER);
        authenticateAs(me);

        ProfileDTO result = profileService.getMyProfile();

        assertEquals(1L, result.getId());
        assertEquals("test1@example.com", result.getEmail());
        assertEquals("USER", result.getRole());
    }

    @Test
    void getMyProfile_notAuthenticated_throws401() {
        // No authentication set
        assertThrows(ResponseStatusException.class, () -> profileService.getMyProfile());
    }

    @Test
    void updateMyProfile_validData_updatesSuccessfully() {
        Profile me = createTestProfile(1L, Role.USER);
        authenticateAs(me);

        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        dto.setUsername("NewUsername");
        dto.setBio("New bio");
        dto.setCity("Berlin");

        when(profileRepository.existsByUsername("NewUsername")).thenReturn(false);
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileDTO result = profileService.updateMyProfile(dto);

        assertEquals("NewUsername", result.getUsername());
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void updateMyProfile_duplicateUsername_throws409() {
        Profile me = createTestProfile(1L, Role.USER);
        authenticateAs(me);

        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        dto.setUsername("TakenUsername");

        when(profileRepository.existsByUsername("TakenUsername")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> profileService.updateMyProfile(dto));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void updateMyProfile_sameUsername_noConflict() {
        Profile me = createTestProfile(1L, Role.USER);
        me.setUsernameDisplay("SameUsername");
        authenticateAs(me);

        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        dto.setUsername("SameUsername"); // Same as current

        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileDTO result = profileService.updateMyProfile(dto);

        assertEquals("SameUsername", result.getUsername());
        verify(profileRepository, never()).existsByUsername(anyString());
    }

    @Test
    void uploadAvatar_validImage_uploadsSuccessfully() {
        Profile me = createTestProfile(1L, Role.USER);
        authenticateAs(me);

        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", new byte[]{1, 2, 3}
        );

        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileDTO result = profileService.uploadAvatar(file);

        assertNotNull(result);
        verify(minioService).upload(anyString(), eq(file));
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void uploadAvatar_invalidType_throws400() {
        Profile me = createTestProfile(1L, Role.USER);
        authenticateAs(me);

        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", new byte[]{1, 2, 3}
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> profileService.uploadAvatar(file));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void uploadAvatar_emptyFile_throws400() {
        Profile me = createTestProfile(1L, Role.USER);
        authenticateAs(me);

        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[]{}
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> profileService.uploadAvatar(file));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void deleteMyProfile_deletesAllData() {
        Profile me = createTestProfile(1L, Role.USER);
        me.setAvatarObjectKey("avatars/1/test.png");
        authenticateAs(me);

        when(postRepository.findByAuthorId(1L)).thenReturn(Collections.emptyList());

        profileService.deleteMyProfile();

        verify(postRepository).deleteByAuthorId(1L);
        verify(minioService).delete("avatars/1/test.png");
        verify(profileRepository).delete(me);
    }

    // ==================== ADMIN ENDPOINTS ====================

    @Test
    void getProfileForAdmin_existingId_returnsFullProfile() {
        Profile profile = createTestProfile(1L, Role.USER);

        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));

        ProfileDTO result = profileService.getProfileForAdmin(1L);

        assertEquals(1L, result.getId());
        assertEquals("test1@example.com", result.getEmail());
        assertEquals("USER", result.getRole());
    }

    @Test
    void changeRole_validRequest_changesRole() {
        Profile admin = createTestProfile(1L, Role.ADMIN);
        authenticateAs(admin);

        Profile targetUser = createTestProfile(2L, Role.USER);

        when(profileRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileDTO result = profileService.changeRole(2L, Role.ADMIN);

        assertEquals("ADMIN", result.getRole());
        verify(profileRepository).save(targetUser);
    }

    @Test
    void changeRole_ownAccount_throws400() {
        Profile admin = createTestProfile(1L, Role.ADMIN);
        authenticateAs(admin);

        when(profileRepository.findById(1L)).thenReturn(Optional.of(admin));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> profileService.changeRole(1L, Role.USER));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("own role"));
    }

    @Test
    void deleteProfileAsAdmin_deletesUser() {
        Profile admin = createTestProfile(1L, Role.ADMIN);
        authenticateAs(admin);

        Profile targetUser = createTestProfile(2L, Role.USER);

        when(profileRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(postRepository.findByAuthorId(2L)).thenReturn(Collections.emptyList());

        profileService.deleteProfileAsAdmin(2L);

        verify(postRepository).deleteByAuthorId(2L);
        verify(profileRepository).delete(targetUser);
    }

    @Test
    void deleteProfileAsAdmin_ownAccount_throws400() {
        Profile admin = createTestProfile(1L, Role.ADMIN);
        authenticateAs(admin);

        when(profileRepository.findById(1L)).thenReturn(Optional.of(admin));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> profileService.deleteProfileAsAdmin(1L));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("own account"));
    }

    @Test
    void testGetMyProfile() {
        assertNotNull(profileService);
    }

    @Test
    void testGetAllProfilesPublic() {
        Profile p1 = new Profile();
        p1.setId(1L);

        when(profileRepository.findAll()).thenReturn(List.of(p1));

        var result = profileService.getAllProfilesPublic();

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
    }

    @Test
    void testGetProfileForAdmin() {
        Profile p = new Profile();
        p.setId(1L);

        when(profileRepository.findById(1L)).thenReturn(Optional.of(p));

        var result = profileService.getProfileForAdmin(1L);

        assertEquals(1L, result.getId());
    }

}