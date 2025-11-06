package at.technikum.springrestbackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generiere JWT Token für User
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Generiere JWT Token mit custom claims
     */
    public String generateToken(String username, Map<String, Object> claims) {
        return createToken(claims, username);
    }

    /**
     * Erstelle JWT Token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extrahiere Username aus JWT Token
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * Extrahiere Expiry Date aus JWT Token
     */
    public Date getExpiryDateFromJWT(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * Extrahiere alle Claims aus JWT Token
     */
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Prüfe ob JWT Token noch valid ist
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Prüfe ob Token expired ist
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpiryDateFromJWT(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
