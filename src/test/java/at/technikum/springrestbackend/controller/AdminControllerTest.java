package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.config.TestSecurityConfig;
import at.technikum.springrestbackend.dto.ProfileDTO;
import at.technikum.springrestbackend.dto.RoleUpdateDTO;
import at.technikum.springrestbackend.entity.Role;
import at.technikum.springrestbackend.exception.GlobalExceptionHandler;
import at.technikum.springrestbackend.security.JwtAuthenticationFilter;
import at.technikum.springrestbackend.service.PostService;
import at.technikum.springrestbackend.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminController")
class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ProfileService profileService;
    @MockitoBean
    PostService postService;

    // ✅ важливо: щоб Spring НЕ створював реальний JwtAuthenticationFilter (і не падав на JwtService)
    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Nested
    @DisplayName("GET /api/admin/users")
    @WithMockUser(roles = "ADMIN")
    class GetAllUsers {
        @Test
        @DisplayName("200 OK")
        void getAllUsers_returnsPage() throws Exception {
            ProfileDTO profile = createProfileDTO(1L, "testuser");
            Page<ProfileDTO> page = new PageImpl<>(List.of(profile));

            when(profileService.getAllProfilesForAdmin(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].email").exists());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/users/{id}/role")
    @WithMockUser(roles = "ADMIN")
    class ChangeRole {
        @Test
        @DisplayName("200 OK")
        void changeRole_valid_returns200() throws Exception {
            RoleUpdateDTO request = new RoleUpdateDTO(Role.ADMIN);
            ProfileDTO response = createProfileDTO(1L, "user");

            when(profileService.changeRole(eq(1L), eq(Role.ADMIN))).thenReturn(response);

            mockMvc.perform(put("/api/admin/users/1/role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("400 Bad Request")
        void changeRole_ownAccount_returns400() throws Exception {
            RoleUpdateDTO request = new RoleUpdateDTO(Role.USER);

            when(profileService.changeRole(eq(1L), any()))
                    .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST));

            mockMvc.perform(put("/api/admin/users/1/role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/users/{id}")
    @WithMockUser(roles = "ADMIN")
    class DeleteUser {
        @Test
        @DisplayName("204 No Content")
        void deleteUser_returns204() throws Exception {
            doNothing().when(profileService).deleteProfileAsAdmin(1L);

            mockMvc.perform(delete("/api/admin/users/1").with(csrf()))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/posts/{id}")
    @WithMockUser(roles = "ADMIN")
    class DeletePost {
        @Test
        @DisplayName("204 No Content")
        void deletePost_returns204() throws Exception {
            doNothing().when(postService).deleteAsAdmin(1L);

            mockMvc.perform(delete("/api/admin/posts/1").with(csrf()))
                    .andExpect(status().isNoContent());
        }
    }

    private ProfileDTO createProfileDTO(Long id, String username) {
        return new ProfileDTO(
                id, username, "test@example.com", "AT", "Bio", 25, "Vienna",
                "male", "avatar.png", "USER", LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
