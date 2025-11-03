package com.finger.hand_backend.auth;

import com.finger.hand_backend.dto.LoginRequest;
import com.finger.hand_backend.security.JwtTokenProvider;
import com.finger.hand_backend.user.entity.User;
import com.finger.hand_backend.user.repository.UserRepository;
import com.finger.hand_backend.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final long validityMs;

    public AuthService(UserService userService,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       @Value("${jwt.access-validity-ms}") long validityMs) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.validityMs = validityMs;
    }

    public User signup(String email, String password) {
        return userService.signup(email, password);
    }

    public String login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("INVALID_CREDENTIALS"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("INVALID_CREDENTIALS");
        }
        return tokenProvider.generateAccessToken(user.getId(), user.getEmail());
    }

    public long getExpiresInSeconds() {
        return validityMs / 1000;
    }
}

