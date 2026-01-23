package at.technikum.springrestbackend.security;

import at.technikum.springrestbackend.entity.Profile;
import at.technikum.springrestbackend.repository.ProfileRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final ProfileRepository profileRepository;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   ProfileRepository profileRepository) {
        this.jwtService = jwtService;
        this.profileRepository = profileRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = getBearerToken(request);
        if (token != null) {
            try {
                authenticate(token);
            } catch (org.springframework.security.authentication.DisabledException ex) {
                // 403 JSON
                response.setStatus(403);
                response.setContentType("application/json");
                throw ex;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getBearerToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return header.substring(BEARER_PREFIX.length());
    }

    private void authenticate(String token) {
        if (!jwtService.isTokenValid(token)) {
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        String email = jwtService.getEmailFromToken(token);
        Profile profile = profileRepository.findByEmail(email).orElse(null);
        if (profile == null) {
            return;
        }

        if (!profile.isEnabled()) {
            throw new org.springframework.security.authentication.DisabledException("User account is disabled");
        }

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        profile,
                        null,
                        profile.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
