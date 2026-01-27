package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.LoginRequestDTO;
import at.technikum.springrestbackend.dto.LoginResponseDTO;
import at.technikum.springrestbackend.dto.RegisterRequestDTO;
import at.technikum.springrestbackend.entity.Profile;
import at.technikum.springrestbackend.entity.Role;
import at.technikum.springrestbackend.repository.ProfileRepository;
import at.technikum.springrestbackend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    ProfileRepository profileRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtService jwtService;

    @InjectMocks
    AuthService authService;

    private RegisterRequestDTO baseRegister() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setUsername("Markiyan");
        dto.setEmail("test@example.com");
        dto.setPassword("Password1");
        dto.setConfirmPassword("Password1");
        dto.setCountry("AT");
        return dto;
    }

    private LoginRequestDTO loginDto(String identifier, String password) {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail(identifier);
        dto.setPassword(password);
        return dto;
    }

    private Profile profile(String email, String username, boolean enabled, Role role, String hash) {
        Profile p = new Profile();
        p.setId(1L);
        p.setEmail(email);
        p.setUsernameDisplay(username);
        p.setEnabled(enabled);
        p.setRole(role);
        p.setPasswordHash(hash);
        return p;
    }

    @Test
    void register_valid_savesProfile_andReturnsToken() {
        RegisterRequestDTO dto = baseRegister();
        dto.setEmail("  TEST@Example.COM ");
        dto.setCountry(" at ");
        dto.setUsername("  Markiyan  ");

        when(profileRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(profileRepository.existsByUsername("Markiyan")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("HASH");
        when(jwtService.generateToken("test@example.com")).thenReturn("TOKEN");

        authService.register(dto);

        ArgumentCaptor<Profile> cap = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository).save(cap.capture());
        Profile saved = cap.getValue();

        assertEquals("test@example.com", saved.getEmail());
        assertEquals("Markiyan", saved.getUsernameDisplay());
        assertEquals("AT", saved.getCountry());
        assertEquals("HASH", saved.getPasswordHash());
        assertEquals(Role.USER, saved.getRole());
        assertTrue(saved.isEnabled());

        verify(jwtService).generateToken("test@example.com");
    }

    @Test
    void register_emailMissing_throws400() {
        RegisterRequestDTO dto = baseRegister();
        dto.setEmail("   ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Email is required", ex.getReason());
    }

    @Test
    void register_usernameMissing_throws400() {
        RegisterRequestDTO dto = baseRegister();
        dto.setUsername(" ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Username is required", ex.getReason());
    }

    @Test
    void register_usernameTooShort_throws400() {
        RegisterRequestDTO dto = baseRegister();
        dto.setUsername("abcd");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Username must be at least"));
    }

    @Test
    void register_passwordMissing_throws400() {
        RegisterRequestDTO dto = baseRegister();
        dto.setPassword(" ");
        dto.setConfirmPassword(" ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Password is required", ex.getReason());
    }

    @Test
    void register_passwordPolicyFail_throws400() {
        RegisterRequestDTO dto = baseRegister();
        dto.setPassword("password1");
        dto.setConfirmPassword("password1");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Password must be at least"));
    }

    @Test
    void register_passwordMismatch_throws400() {
        RegisterRequestDTO dto = baseRegister();
        dto.setConfirmPassword("Other1");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Passwords do not match", ex.getReason());
    }

    @Test
    void register_countryMissing_throws400() {
        RegisterRequestDTO dto = baseRegister();
        dto.setCountry(" ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Country is required", ex.getReason());
    }

    @Test
    void register_countryInvalid_throws400() {
        RegisterRequestDTO dto = baseRegister();
        dto.setCountry("ZZ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Country must be a valid ISO code"));
    }

    @Test
    void register_emailExists_throws409() {
        RegisterRequestDTO dto = baseRegister();
        when(profileRepository.existsByEmail("test@example.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(dto));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Email already registered", ex.getReason());
    }

    @Test
    void register_usernameExists_throws409() {
        RegisterRequestDTO dto = baseRegister();
        when(profileRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(profileRepository.existsByUsername("Markiyan")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(dto));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Username already taken", ex.getReason());
    }

    @Test
    void login_identifierMissing_throws400() {
        LoginRequestDTO dto = loginDto("   ", "Password1");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Identifier is required", ex.getReason());
    }

    @Test
    void login_emailIdentifier_userNotFound_throws401() {
        LoginRequestDTO dto = loginDto("  TEST@Example.COM ", "Password1");
        when(profileRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(dto));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Invalid credentials", ex.getReason());
        verify(profileRepository).findByEmail("test@example.com");
    }

    @Test
    void login_usernameIdentifier_userNotFound_throws401() {
        LoginRequestDTO dto = loginDto("  user01  ", "Password1");
        when(profileRepository.findByUsername("user01")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(dto));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(profileRepository).findByUsername("user01");
    }

    @Test
    void login_disabledUser_throws401() {
        LoginRequestDTO dto = loginDto("user01", "Password1");
        Profile p = profile("u@example.com", "user01", false, Role.USER, "HASH");

        when(profileRepository.findByUsername("user01")).thenReturn(Optional.of(p));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(dto));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void login_wrongPassword_throws401() {
        LoginRequestDTO dto = loginDto("user01", "Password1");
        Profile p = profile("u@example.com", "user01", true, Role.USER, "HASH");

        when(profileRepository.findByUsername("user01")).thenReturn(Optional.of(p));
        when(passwordEncoder.matches("Password1", "HASH")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(dto));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void login_success_usernameIdentifier_returnsTokenAndRole() {
        LoginRequestDTO dto = loginDto("user01", "Password1");
        Profile p = profile("u@example.com", "user01", true, Role.USER, "HASH");

        when(profileRepository.findByUsername("user01")).thenReturn(Optional.of(p));
        when(passwordEncoder.matches("Password1", "HASH")).thenReturn(true);
        when(jwtService.generateToken("u@example.com")).thenReturn("TOKEN");

        LoginResponseDTO res = authService.login(dto);

        assertEquals("TOKEN", res.getToken());
        assertEquals("user01", res.getUsername());
        assertEquals("u@example.com", res.getEmail());
        assertEquals("USER", res.getRole());
    }
}
