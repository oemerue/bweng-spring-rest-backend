package at.technikum.springrestbackend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SecurityErrorResponseTest.TestController.class)
@Import({
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        SecurityApiErrorWriter.class,
        SecurityErrorResponseTest.TestSecurityOnlyConfig.class
})
class SecurityErrorResponseTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unused")
    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;


    @RestController
    public static class TestController {

        @GetMapping("/api/secure-test")
        public String secure() {
            return "ok";
        }

        @GetMapping("/api/admin/test")
        public String admin() {
            return "admin";
        }
    }

    @TestConfiguration
    static class TestSecurityOnlyConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(
                HttpSecurity http,
                JsonAuthenticationEntryPoint entryPoint,
                JsonAccessDeniedHandler deniedHandler
        ) throws Exception {

            http.csrf(csrf -> csrf.disable());
            http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

            http.exceptionHandling(ex -> ex
                    .authenticationEntryPoint(entryPoint)
                    .accessDeniedHandler(deniedHandler)
            );

            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            );

            return http.build();
        }
    }

    @Test
    void anonymous_returns401_jsonApiError() throws Exception {
        mockMvc.perform(get("/api/secure-test"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/secure-test"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userRole_onAdminPath_returns403_jsonApiError() throws Exception {
        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Forbidden"))
                .andExpect(jsonPath("$.path").value("/api/admin/test"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
