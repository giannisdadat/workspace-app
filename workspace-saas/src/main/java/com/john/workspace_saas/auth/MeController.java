package com.john.workspace_saas.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.john.workspace_saas.user.UserRepository;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final UserRepository userRepository;

    @GetMapping
    public UserResponse me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userRepository.findById(userId)
                .map(u -> new UserResponse(u.getId(), u.getEmail(), u.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}