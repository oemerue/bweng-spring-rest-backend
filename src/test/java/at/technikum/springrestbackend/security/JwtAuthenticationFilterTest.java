package at.technikum.springrestbackend.security;

import at.technikum.springrestbackend.entity.Profile;
import at.technikum.springrestbackend.entity.Role;
import at.technikum.springrestbackend.repository.ProfileRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter - HTTP Request Authentication")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Valid token sets Authentication in SecurityContext")
    void doFilterInternal_validToken_setsAuthentication() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String email = "user@example.com";

        Profile profile = createTestProfile(1L, email, Role.USER);

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(profileRepository.findByEmail(email)).thenReturn(Optional.of(profile));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication, "Authentication must be set");
        assertEquals(profile, authentication.getPrincipal(), "Principal must be the Profile");
    }

    @Test
    @DisplayName("Request without token continues the chain without authentication")
    void doFilterInternal_noToken_continuesWithoutAuthentication() throws ServletException, IOException {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Authentication must not be set without a token");
    }

    @Test
    @DisplayName("Header without 'Bearer ' prefix is ignored")
    void doFilterInternal_noBearerPrefix_continuesWithoutAuthentication() throws ServletException, IOException {
        request.addHeader("Authorization", "Basic sometoken");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Invalid token continues the chain without authentication")
    void doFilterInternal_invalidToken_continuesWithoutAuthentication() throws ServletException, IOException {
        String invalidToken = "invalid.token";
        request.addHeader("Authorization", "Bearer " + invalidToken);

        when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).getEmailFromToken(anyString());
    }

    @Test
    @DisplayName("Token is valid, but user does not exist in the database")
    void doFilterInternal_userNotFound_continuesWithoutAuthentication() throws ServletException, IOException {
        String token = "valid.token";
        String email = "deleted@example.com";

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(profileRepository.findByEmail(email)).thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Authentication must not be set if the user is missing");
    }

    private Profile createTestProfile(Long id, String email, Role role) {
        Profile profile = new Profile();
        profile.setId(id);
        profile.setEmail(email);
        profile.setUsernameDisplay("TestUser");
        profile.setRole(role);
        profile.setPasswordHash("hash");
        profile.setCountry("AT");
        return profile;
    }
}
