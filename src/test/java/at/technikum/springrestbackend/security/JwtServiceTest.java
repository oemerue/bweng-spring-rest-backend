package at.technikum.springrestbackend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService - JWT Token Management")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET = "test-secret-key-minimum-32-characters-long-for-hs256";
    private static final long TEST_EXPIRATION_MS = 3600000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", TEST_EXPIRATION_MS);
        ReflectionTestUtils.invokeMethod(jwtService, "init");
    }

    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("Returns a valid JWT token")
        void generateToken_validEmail_returnsJwtToken() {
            String email = "user@example.com";

            String token = jwtService.generateToken(email);

            assertNotNull(token);
            String[] parts = token.split("\\.");
            assertEquals(3, parts.length, "JWT must have 3 parts");
        }

        @Test
        @DisplayName("Different emails produce different tokens")
        void generateToken_differentEmails_returnsDifferentTokens() {
            String token1 = jwtService.generateToken("user1@example.com");
            String token2 = jwtService.generateToken("user2@example.com");

            assertNotEquals(token1, token2);
        }
    }

    @Nested
    @DisplayName("getEmailFromToken()")
    class GetEmailFromToken {

        @Test
        @DisplayName("Returns email from a valid token")
        void getEmailFromToken_validToken_returnsEmail() {
            String originalEmail = "test@example.com";
            String token = jwtService.generateToken(originalEmail);

            String extractedEmail = jwtService.getEmailFromToken(token);

            assertEquals(originalEmail, extractedEmail);
        }

        @Test
        @DisplayName("Throws exception for an invalid token")
        void getEmailFromToken_invalidToken_throwsException() {
            String invalidToken = "invalid.token.here";

            assertThrows(RuntimeException.class,
                    () -> jwtService.getEmailFromToken(invalidToken));
        }
    }

    @Nested
    @DisplayName("isTokenValid()")
    class IsTokenValid {

        @Test
        @DisplayName("Returns true for a valid token")
        void isTokenValid_validToken_returnsTrue() {
            String token = jwtService.generateToken("user@example.com");

            assertTrue(jwtService.isTokenValid(token));
        }

        @Test
        @DisplayName("Returns false for an invalid token")
        void isTokenValid_invalidToken_returnsFalse() {
            assertFalse(jwtService.isTokenValid("invalid.token.here"));
        }

        @Test
        @DisplayName("Returns false for a tampered token")
        void isTokenValid_tamperedToken_returnsFalse() {
            String token = jwtService.generateToken("user@example.com");
            String tamperedToken = token.substring(0, token.length() - 1) + "X";

            assertFalse(jwtService.isTokenValid(tamperedToken));
        }

        @Test
        @DisplayName("Returns false for an expired token")
        void isTokenValid_expiredToken_returnsFalse() throws InterruptedException {
            JwtService shortLivedService = new JwtService();
            ReflectionTestUtils.setField(shortLivedService, "secret", TEST_SECRET);
            ReflectionTestUtils.setField(shortLivedService, "expirationMs", 1L);
            ReflectionTestUtils.invokeMethod(shortLivedService, "init");

            String token = shortLivedService.generateToken("user@example.com");
            Thread.sleep(50);

            assertFalse(shortLivedService.isTokenValid(token));
        }

        @Test
        @DisplayName("Returns false for a null token")
        void isTokenValid_nullToken_returnsFalse() {
            try {
                boolean result = jwtService.isTokenValid(null);
                assertFalse(result);
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("null or empty"));
            }
        }

        @Test
        @DisplayName("Returns false for an empty token")
        void isTokenValid_emptyToken_returnsFalse() {
            try {
                boolean result = jwtService.isTokenValid("");
                assertFalse(result);
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("null or empty"));
            }
        }
    }
}
