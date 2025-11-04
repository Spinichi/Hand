package com.finger.hand_backend.auth.controller;

import com.finger.hand_backend.auth.service.AuthService;
import com.finger.hand_backend.common.dto.ApiResponse;
import com.finger.hand_backend.auth.dto.LoginRequest;
import com.finger.hand_backend.auth.dto.SignupRequest;
import com.finger.hand_backend.user.entity.User;
import com.finger.hand_backend.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    // ✅ 회원가입
    @PostMapping("/users/signup")
    public ResponseEntity<ApiResponse<Map<String, Long>>> signup(@Valid @RequestBody SignupRequest request) {
        User created = authService.signup(request.getEmail(), request.getPassword());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        Map.of("id", created.getId()),
                        "회원가입 성공"
                ));
    }

    // ✅ 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        long expiresIn = authService.getExpiresInSeconds();

        return ResponseEntity.ok(
                ApiResponse.success(
                        Map.of(
                                "accessToken", token,
                                "expiresIn", expiresIn
                        ),
                        "로그인 성공"
                )
        );
    }

    // ✅ 회원탈퇴 (DB에서 완전 삭제)
    @DeleteMapping("/users/me")
    public ResponseEntity<ApiResponse<?>> deleteMyAccount(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("Unauthorized"));
        }

        Long userId = Long.valueOf(authentication.getName());
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("User not found"));
        }

        userRepository.deleteById(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(null, "회원탈퇴 완료"));
    }
}
