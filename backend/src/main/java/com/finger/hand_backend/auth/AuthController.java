package com.finger.hand_backend.auth;

import com.finger.hand_backend.dto.LoginRequest;
import com.finger.hand_backend.dto.SignupRequest;
import com.finger.hand_backend.user.User;
import com.finger.hand_backend.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/users/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        User created = authService.signup(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body("{\"id\":"+created.getId()+"}");
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        long expiresIn = authService.getExpiresInSeconds();

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token) // 👈 헤더에 추가
                .header("Expires-In", String.valueOf(expiresIn)) // 선택
                .build();
    }

    // 회원탈퇴 (DB에서 완전 삭제)
    @DeleteMapping("/users/me")
    public ResponseEntity<?> deleteMyAccount(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body("{\"message\":\"Unauthorized\"}");
        }

        Long userId = Long.valueOf(authentication.getName());
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(404).body("{\"message\":\"User not found\"}");
        }

        userRepository.deleteById(userId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

}

