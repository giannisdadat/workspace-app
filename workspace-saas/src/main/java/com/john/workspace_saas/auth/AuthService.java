package com.john.workspace_saas.auth;

import com.john.workspace_saas.user.user;
import com.john.workspace_saas.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        user user = new user();
        user.setEmail(email);
        user.setName(request.name().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        user saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getEmail(), saved.getName());
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        user user = userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token,
                new UserResponse(user.getId(), user.getEmail(), user.getName()));
    }
}