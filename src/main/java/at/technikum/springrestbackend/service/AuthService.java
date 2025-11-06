package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.AuthResponseDTO;
import at.technikum.springrestbackend.dto.UserLoginRequestDTO;
import at.technikum.springrestbackend.dto.UserRegisterRequestDTO;

public interface AuthService {
    AuthResponseDTO register(UserRegisterRequestDTO request);
    AuthResponseDTO login(UserLoginRequestDTO request);
}
