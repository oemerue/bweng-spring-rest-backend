package at.technikum.springrestbackend.util;

import at.technikum.springrestbackend.entity.Profile;
import at.technikum.springrestbackend.entity.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilTest {

    private Profile createProfile(Long id, Role role) {
        Profile profile = new Profile();
        profile.setId(id);
        profile.setEmail("test@example.com");
        profile.setUsernameDisplay("TestUser");
        profile.setRole(role);
        profile.setPasswordHash("hash");
        profile.setCountry("AT");
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

    // ==================== currentProfileOrThrow ====================

    @Test
    void currentProfileOrThrow_authenticated_returnsProfile() {
        Profile profile = createProfile(1L, Role.USER);
        authenticateAs(profile);

        Profile result = SecurityUtil.currentProfileOrThrow();

        assertEquals(1L, result.getId());
        assertEquals("TestUser", result.getUsernameDisplay());
    }

    @Test
    void currentProfileOrThrow_notAuthenticated_throws401() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                SecurityUtil::currentProfileOrThrow);

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Not authenticated"));
    }

    @Test
    void currentProfileOrThrow_nullAuthentication_throws401() {
        SecurityContextHolder.getContext().setAuthentication(null);

        assertThrows(ResponseStatusException.class, SecurityUtil::currentProfileOrThrow);
    }

    // ==================== currentProfile ====================

    @Test
    void currentProfile_authenticated_returnsOptionalWithProfile() {
        Profile profile = createProfile(1L, Role.USER);
        authenticateAs(profile);

        Optional<Profile> result = SecurityUtil.currentProfile();

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void currentProfile_notAuthenticated_returnsEmpty() {
        Optional<Profile> result = SecurityUtil.currentProfile();

        assertTrue(result.isEmpty());
    }

    // ==================== isAdmin ====================

    @Test
    void isAdmin_adminRole_returnsTrue() {
        Profile admin = createProfile(1L, Role.ADMIN);

        assertTrue(SecurityUtil.isAdmin(admin));
    }

    @Test
    void isAdmin_userRole_returnsFalse() {
        Profile user = createProfile(1L, Role.USER);

        assertFalse(SecurityUtil.isAdmin(user));
    }

    @Test
    void isAdmin_null_returnsFalse() {
        assertFalse(SecurityUtil.isAdmin(null));
    }

    // ==================== isCurrentUserAdmin ====================

    @Test
    void isCurrentUserAdmin_adminAuthenticated_returnsTrue() {
        Profile admin = createProfile(1L, Role.ADMIN);
        authenticateAs(admin);

        assertTrue(SecurityUtil.isCurrentUserAdmin());
    }

    @Test
    void isCurrentUserAdmin_userAuthenticated_returnsFalse() {
        Profile user = createProfile(1L, Role.USER);
        authenticateAs(user);

        assertFalse(SecurityUtil.isCurrentUserAdmin());
    }

    @Test
    void isCurrentUserAdmin_notAuthenticated_returnsFalse() {
        assertFalse(SecurityUtil.isCurrentUserAdmin());
    }

    // ==================== ensureOwnerOrAdmin ====================

    @Test
    void ensureOwnerOrAdmin_isOwner_noException() {
        Profile user = createProfile(1L, Role.USER);
        authenticateAs(user);

        assertDoesNotThrow(() -> SecurityUtil.ensureOwnerOrAdmin(1L));
    }

    @Test
    void ensureOwnerOrAdmin_isAdmin_noException() {
        Profile admin = createProfile(1L, Role.ADMIN);
        authenticateAs(admin);

        // Admin can access any resource
        assertDoesNotThrow(() -> SecurityUtil.ensureOwnerOrAdmin(999L));
    }

    @Test
    void ensureOwnerOrAdmin_notOwnerNotAdmin_throws403() {
        Profile user = createProfile(1L, Role.USER);
        authenticateAs(user);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> SecurityUtil.ensureOwnerOrAdmin(2L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Access denied"));
    }

    @Test
    void ensureOwnerOrAdmin_notAuthenticated_throws401() {
        assertThrows(ResponseStatusException.class,
                () -> SecurityUtil.ensureOwnerOrAdmin(1L));
    }
}