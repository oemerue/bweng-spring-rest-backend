package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.constant.AppConstants;
import at.technikum.springrestbackend.dto.LoginRequestDTO;
import at.technikum.springrestbackend.dto.LoginResponseDTO;
import at.technikum.springrestbackend.dto.RegisterRequestDTO;
import at.technikum.springrestbackend.entity.Profile;
import at.technikum.springrestbackend.entity.Role;
import at.technikum.springrestbackend.repository.ProfileRepository;
import at.technikum.springrestbackend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final int MIN_USERNAME_LENGTH = 5;
    private static final int MIN_PASSWORD_LENGTH = 8;

    private static final Pattern PASSWORD_POLICY = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{"
                    + MIN_PASSWORD_LENGTH
                    + ",}$"
    );

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(ProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO register(RegisterRequestDTO dto) {
        final String email = normalizeEmail(dto.getEmail());
        final String username = normalizeUsername(dto.getUsername());
        final String country = normalizeCountry(dto.getCountry());

        validateRegisterInput(
                email,
                username,
                country,
                dto.getPassword(),
                dto.getConfirmPassword()
        );

        Profile profile = createProfile(email, username, country, dto.getPassword());
        profileRepository.save(profile);

        return createLoginResponse(profile);
    }

    public LoginResponseDTO login(LoginRequestDTO dto) {
        final String identifier = normalizeIdentifier(dto.getEmail());

        Profile profile = findByIdentifier(identifier)
                .orElseThrow(this::unauthorizedInvalidCredentials);

        if (!profile.isEnabled()) {
            throw unauthorizedInvalidCredentials();
        }

        if (!passwordEncoder.matches(dto.getPassword(), profile.getPasswordHash())) {
            throw unauthorizedInvalidCredentials();
        }

        return createLoginResponse(profile);
    }

    private void validateRegisterInput(String email,
                                       String username,
                                       String country,
                                       String password,
                                       String confirmPassword) {

        validateUsername(username);
        validatePasswordPresent(password);
        validatePasswordPolicy(password);
        validatePasswordsMatch(password, confirmPassword);
        validateCountry(country);
        validateUniqueness(email, username);
    }

    private void validateUsername(String username) {
        if (username.length() < MIN_USERNAME_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username must be at least "
                            + MIN_USERNAME_LENGTH
                            + " characters long"
            );
        }
    }

    private void validatePasswordPresent(String password) {
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password is required"
            );
        }
    }

    private void validatePasswordPolicy(String password) {
        if (!PASSWORD_POLICY.matcher(password).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must be at least "
                            + MIN_PASSWORD_LENGTH
                            + " chars and contain at least 1 uppercase, "
                            + "1 lowercase and 1 number"
            );
        }
    }

    private void validatePasswordsMatch(String password, String confirmPassword) {
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passwords do not match"
            );
        }
    }

    private void validateCountry(String country) {
        if (!AppConstants.VALID_ISO_COUNTRIES.contains(country)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Country must be a valid ISO code (e.g., AT, DE)"
            );
        }
    }

    private void validateUniqueness(String email, String username) {
        if (profileRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already registered"
            );
        }
        if (profileRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already taken"
            );
        }
    }

    private ResponseStatusException unauthorizedInvalidCredentials() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials"
        );
    }

    private Optional<Profile> findByIdentifier(String identifier) {
        if (identifier.contains("@")) {
            return profileRepository.findByEmail(normalizeEmail(identifier));
        }
        return profileRepository.findByUsername(normalizeUsername(identifier));
    }

    private Profile createProfile(String email,
                                  String username,
                                  String country,
                                  String rawPassword) {
        Profile profile = new Profile();
        profile.setEmail(email);
        profile.setUsernameDisplay(username);
        profile.setCountry(country);
        profile.setPasswordHash(passwordEncoder.encode(rawPassword));
        profile.setRole(Role.USER);
        profile.setEnabled(true);
        return profile;
    }

    private LoginResponseDTO createLoginResponse(Profile profile) {
        String token = jwtService.generateToken(profile.getEmail());
        return new LoginResponseDTO(
                token,
                profile.getUsernameDisplay(),
                profile.getEmail(),
                profile.getRole().name()
        );
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is required"
            );
        }
        return email.trim().toLowerCase();
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username is required"
            );
        }
        return username.trim();
    }

    private String normalizeCountry(String country) {
        if (country == null || country.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Country is required"
            );
        }
        return country.trim().toUpperCase();
    }

    private String normalizeIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Identifier is required"
            );
        }
        return identifier.trim();
    }
}
