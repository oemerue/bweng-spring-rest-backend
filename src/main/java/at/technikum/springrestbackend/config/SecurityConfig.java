package at.technikum.springrestbackend.config;

import at.technikum.springrestbackend.security.JwtAuthenticationFilter;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import at.technikum.springrestbackend.security.JsonAccessDeniedHandler;
import at.technikum.springrestbackend.security.JsonAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JsonAuthenticationEntryPoint authenticationEntryPoint,
            JsonAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.exceptionHandling(this::configureExceptionHandling);
        http.authorizeHttpRequests(this::configureAuthorization);
        http.addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void configureExceptionHandling(ExceptionHandlingConfigurer<HttpSecurity> ex) {
        ex.authenticationEntryPoint(authenticationEntryPoint);
        ex.accessDeniedHandler(accessDeniedHandler);
    }

    private void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>
                    .AuthorizationManagerRequestMatcherRegistry auth) {

        auth.requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll();

        auth.requestMatchers("/api/profiles/me/**").authenticated();
        auth.requestMatchers(HttpMethod.GET, "/api/profiles", "/api/profiles/**")
                .permitAll();

        auth.requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/**").permitAll();
        auth.requestMatchers(HttpMethod.POST, "/api/posts", "/api/posts/**").authenticated();
        auth.requestMatchers(HttpMethod.PUT, "/api/posts", "/api/posts/**").authenticated();
        auth.requestMatchers(HttpMethod.DELETE, "/api/posts", "/api/posts/**").authenticated();

        auth.requestMatchers(HttpMethod.GET, "/api/files/**").permitAll();

        auth.requestMatchers("/api/admin/**").hasRole("ADMIN");

        auth.requestMatchers(
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**"
        ).permitAll();

        auth.anyRequest().authenticated();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:3000"
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Location"));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
