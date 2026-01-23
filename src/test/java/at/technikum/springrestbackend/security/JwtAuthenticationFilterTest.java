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

/**
 * Тести для JWT Authentication Filter.
 * <p>
 * Цей фільтр перехоплює кожен HTTP запит і:
 * 1. Витягує JWT токен з заголовка Authorization
 * 2. Валідує токен
 * 3. Завантажує користувача з БД
 * 4. Встановлює Authentication в SecurityContext
 */
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
        // Важливо очищати SecurityContext після кожного тесту!
        SecurityContextHolder.clearContext();
    }

    // ==================== УСПІШНА АУТЕНТИФІКАЦІЯ ====================

    @Test
    @DisplayName("Валідний токен встановлює Authentication в SecurityContext")
    void doFilterInternal_validToken_setsAuthentication() throws ServletException, IOException {
        // Given
        String token = "valid. jwt.token";
        String email = "user@example.com";

        Profile profile = createTestProfile(1L, email, Role.USER);

        // Налаштовуємо заголовок Authorization:  Bearer <token>
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(profileRepository.findByEmail(email)).thenReturn(Optional.of(profile));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        // 1. Перевіряємо, що фільтр продовжив ланцюжок
        verify(filterChain).doFilter(request, response);

        // 2. Перевіряємо, що Authentication встановлено
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication, "Authentication повинен бути встановлений");
        assertEquals(profile, authentication.getPrincipal(), "Principal повинен бути Profile");
    }

    // ==================== БЕЗ ТОКЕНА ====================

    @Test
    @DisplayName("Запит без токена продовжує ланцюжок без аутентифікації")
    void doFilterInternal_noToken_continuesWithoutAuthentication() throws ServletException, IOException {
        // Given - запит без заголовка Authorization

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Без токена Authentication не повинен встановлюватися");
    }

    @Test
    @DisplayName("Заголовок без 'Bearer ' префіксу ігнорується")
    void doFilterInternal_noBearerPrefix_continuesWithoutAuthentication() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Basic sometoken");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        // JwtService не повинен викликатися
        verifyNoInteractions(jwtService);
    }

    // ==================== НЕВАЛІДНИЙ ТОКЕН ====================

    @Test
    @DisplayName("Невалідний токен продовжує ланцюжок без аутентифікації")
    void doFilterInternal_invalidToken_continuesWithoutAuthentication() throws ServletException, IOException {
        // Given
        String invalidToken = "invalid.token";
        request.addHeader("Authorization", "Bearer " + invalidToken);

        when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        // getEmailFromToken не викликається для невалідного токена
        verify(jwtService, never()).getEmailFromToken(anyString());
    }

    // ==================== КОРИСТУВАЧ НЕ ЗНАЙДЕНИЙ ====================

    @Test
    @DisplayName("Токен валідний, але користувач не існує в БД")
    void doFilterInternal_userNotFound_continuesWithoutAuthentication() throws ServletException, IOException {
        // Given
        String token = "valid.token";
        String email = "deleted@example.com";

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(profileRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Якщо користувач видалений, Authentication не встановлюється");
    }

    // ==================== HELPER METHODS ====================

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