package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.config.SecurityConfig;
import at.technikum.springrestbackend.config.TestSecurityConfig;
import at.technikum.springrestbackend.dto.ProfileDTO;
import at.technikum.springrestbackend.security.JwtAuthenticationFilter;
import at.technikum.springrestbackend.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ProfileController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
        }
)
@Import(TestSecurityConfig.class)
class ProfileSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProfileService profileService;

    @Test
    void getAllProfiles_anonymous_isOk() throws Exception {
        when(profileService.getAllProfilesPublic()).thenReturn(List.of());

        mockMvc.perform(get("/api/profiles"))
                .andExpect(status().isOk());
    }

    @Test
    void getMyProfile_anonymous_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/profiles/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyProfile_authenticated_isOk() throws Exception {
        ProfileDTO dto = new ProfileDTO(
                1L, "meUser", "me@example.com", "AT",
                null, null, null, null, null,
                "USER", LocalDateTime.now(), LocalDateTime.now()
        );

        when(profileService.getMyProfile()).thenReturn(dto);

        mockMvc.perform(get("/api/profiles/me")
                        .with(user("me@example.com").roles("USER")))
                .andExpect(status().isOk());
    }
}
