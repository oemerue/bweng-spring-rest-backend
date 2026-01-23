package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.config.TestSecurityConfig;
import at.technikum.springrestbackend.dto.ProfileDTO;
import at.technikum.springrestbackend.dto.ProfileUpdateDTO;
import at.technikum.springrestbackend.dto.PublicProfileDTO;
import at.technikum.springrestbackend.exception.GlobalExceptionHandler;
import at.technikum.springrestbackend.security.JwtAuthenticationFilter;
import at.technikum.springrestbackend.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProfileController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ProfileController")
class ProfileControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ProfileService profileService;

    // ✅ важливо: щоб Spring НЕ створював реальний JwtAuthenticationFilter (і не падав на JwtService)
    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Nested
    @DisplayName("GET /api/profiles")
    class GetAllProfiles {
        @Test
        @DisplayName("200 OK - public list")
        void getAllProfiles_returnsList() throws Exception {
            PublicProfileDTO profile = createPublicProfileDTO(1L, "user1");
            when(profileService.getAllProfilesPublic()).thenReturn(List.of(profile));

            mockMvc.perform(get("/api/profiles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("[0].username").value("user1"))
                    .andExpect(jsonPath("[0].email").doesNotExist());
        }
    }

    @Nested
    @DisplayName("GET /api/profiles/{id}")
    class GetProfileById {
        @Test
        @DisplayName("200 OK")
        void getProfile_existing_returnsProfile() throws Exception {
            PublicProfileDTO profile = createPublicProfileDTO(1L, "testuser");
            when(profileService.getProfilePublic(1L)).thenReturn(profile);

            mockMvc.perform(get("/api/profiles/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("testuser"));
        }

        @Test
        @DisplayName("404 Not Found")
        void getProfile_nonExisting_returns404() throws Exception {
            when(profileService.getProfilePublic(999L))
                    .thenThrow(new EntityNotFoundException("Not found"));

            mockMvc.perform(get("/api/profiles/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/profiles/me")
    @WithMockUser
    class GetMyProfile {
        @Test
        @DisplayName("200 OK - full profile")
        void getMyProfile_authenticated_returnsFullProfile() throws Exception {
            ProfileDTO profile = createFullProfileDTO(1L, "testuser", "test@example.com");
            when(profileService.getMyProfile()).thenReturn(profile);

            mockMvc.perform(get("/api/profiles/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }
    }

    @Nested
    @DisplayName("PUT /api/profiles/me")
    @WithMockUser
    class UpdateMyProfile {
        @Test
        @DisplayName("200 OK")
        void updateMyProfile_valid_returns200() throws Exception {
            ProfileUpdateDTO request = new ProfileUpdateDTO();
            request.setUsername("newusername");

            ProfileDTO response = createFullProfileDTO(1L, "newusername", "test@example.com");
            when(profileService.updateMyProfile(any())).thenReturn(response);

            mockMvc.perform(put("/api/profiles/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("newusername"));
        }

        @Test
        @DisplayName("409 Conflict")
        void updateMyProfile_duplicateUsername_returns409() throws Exception {
            ProfileUpdateDTO request = new ProfileUpdateDTO();
            request.setUsername("taken");

            when(profileService.updateMyProfile(any()))
                    .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT));

            mockMvc.perform(put("/api/profiles/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("POST /api/profiles/me/avatar")
    @WithMockUser
    class UploadAvatar {
        @Test
        @DisplayName("200 OK")
        void uploadAvatar_validImage_returns200() throws Exception {
            MockMultipartFile file =
                    new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2, 3});
            ProfileDTO response = createFullProfileDTO(1L, "user", "test@example.com");
            when(profileService.uploadAvatar(any())).thenReturn(response);

            mockMvc.perform(multipart("/api/profiles/me/avatar")
                            .file(file)
                            .with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("400 Bad Request")
        void uploadAvatar_invalidFormat_returns400() throws Exception {
            MockMultipartFile file =
                    new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

            when(profileService.uploadAvatar(any()))
                    .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));

            mockMvc.perform(multipart("/api/profiles/me/avatar")
                            .file(file)
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/profiles/me")
    @WithMockUser
    class DeleteMyProfile {
        @Test
        @DisplayName("204 No Content")
        void deleteMyProfile_returns204() throws Exception {
            doNothing().when(profileService).deleteMyProfile();

            mockMvc.perform(delete("/api/profiles/me").with(csrf()))
                    .andExpect(status().isNoContent());
        }
    }

    private PublicProfileDTO createPublicProfileDTO(Long id, String username) {
        return new PublicProfileDTO(
                id, username, "Bio", 25, "Vienna", "AT", "male", "avatar.png", LocalDateTime.now()
        );
    }

    private ProfileDTO createFullProfileDTO(Long id, String username, String email) {
        return new ProfileDTO(
                id, username, email, "AT", "Bio", 25, "Vienna", "male", "avatar.png",
                "USER", LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
