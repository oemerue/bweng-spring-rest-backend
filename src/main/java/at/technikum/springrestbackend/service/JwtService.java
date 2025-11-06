package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtService {

    // Für Produktion: Secret aus application.properties laden!
    // Mindestens 256 Bit für HS256 (>= 32 Zeichen)
    private final String SECRET = "change-this-super-secret-key-32chars-min!";
    private final long EXPIRATION_MS = 3600_000L; // 1 Stunde

    private SecretKey getSigningKey() {
        // HMAC-Key aus Bytes
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", user.getId());
        claims.put("username", user.getUsername());

        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())   // Eindeutiger Subject: E-Mail
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public long getExpirationMs() {
        return EXPIRATION_MS;
    }
}
