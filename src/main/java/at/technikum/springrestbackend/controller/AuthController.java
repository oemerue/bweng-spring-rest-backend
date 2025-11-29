package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.dto.LoginRequestDTO;
import at.technikum.springrestbackend.dto.LoginResponseDTO;
import at.technikum.springrestbackend.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {

        // 1) User mit Username + Passwort authentifizieren
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username,
                        request.password
                )
        );

        // 2) Authentifizierten User holen
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 3) JWT erzeugen
        String token = jwtService.generateToken(userDetails);

        // 4) Token als JSON zurückgeben
        LoginResponseDTO response = new LoginResponseDTO(token);
        return ResponseEntity.ok(response);
    }
}
