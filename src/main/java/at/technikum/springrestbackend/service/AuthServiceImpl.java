package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.AuthResponseDTO;
import at.technikum.springrestbackend.dto.UserLoginRequestDTO;
import at.technikum.springrestbackend.dto.UserRegisterRequestDTO;
import at.technikum.springrestbackend.entity.User;
import at.technikum.springrestbackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponseDTO register(UserRegisterRequestDTO request) {
        // E-Mail & Username müssen einzigartig sein
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("E-Mail bereits registriert!");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Benutzername bereits vergeben!");
        }

        // User anlegen (Passwort sicher hashen)
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .country(request.getCountry())
                .role(User.Role.USER)
                .build();

        userRepository.save(user);

        // JWT generieren (Subject = Email, Claims: id/username)
        String token = jwtService.generateToken(user);

        return AuthResponseDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .build();
    }

    @Override
    public AuthResponseDTO login(UserLoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden!"));

        // Passwort prüfen (Hash-Vergleich)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Falsches Passwort!");
        }

        String token = jwtService.generateToken(user);

        return AuthResponseDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .build();
    }
}
