package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.dto.*;
import at.technikum.springrestbackend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDTO register(@RequestBody @Valid UserRegisterRequestDTO req) {
        return auth.register(req);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody @Valid UserLoginRequestDTO req) {
        return auth.login(req);
    }
}
