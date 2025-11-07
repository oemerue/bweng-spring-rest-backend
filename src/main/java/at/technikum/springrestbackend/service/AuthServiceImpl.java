package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.AuthResponseDTO;
import at.technikum.springrestbackend.dto.UserLoginRequestDTO;
import at.technikum.springrestbackend.dto.UserRegisterRequestDTO;
import at.technikum.springrestbackend.entity.User;
import at.technikum.springrestbackend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("E-Mail bereits registriert!");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Benutzername bereits vergeben!");
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .country(request.getCountry())
                .role("USER")
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        return AuthResponseDTO.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .build();
    }

    @Override
    public AuthResponseDTO login(UserLoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException(
                        "Benutzer nicht gefunden!"));

        if (!passwordEncoder.matches(request.getPassword(),
                user.getPassword())) {
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
