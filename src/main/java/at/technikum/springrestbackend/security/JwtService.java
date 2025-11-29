package at.technikum.springrestbackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // Einfacher Secret-Key für den Milestone (mind. 32 Zeichen)
    private static final String SECRET = "supergeheimesjwtsecret-supergeheimesjwtsecret";
    private static final long EXPIRATION_MS = 1000 * 60 * 60; // 1 Stunde

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())          // Username in den Token schreiben
                .setIssuedAt(now)                               // Ausstellungszeitpunkt
                .setExpiration(expiry)                          // Ablaufzeit
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // mit Secret signieren
                .compact();                                     // zu String "xxx.yyy.zzz" machen
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);         // subject = username
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())                 // zum Prüfen wieder das Secret
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isExpired(token);
    }

    private boolean isExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }
}
