package com.finger.hand_backend.user.controller;


import com.finger.hand_backend.common.dto.ApiResponse;
import com.finger.hand_backend.user.dto.IndividualUserDtos.*;
import com.finger.hand_backend.user.service.IndividualUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/individual-users")
@RequiredArgsConstructor
public class IndividualUserController {
    private final IndividualUserService service;


    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof Long id) return id;
        if (principal instanceof String s) return Long.valueOf(s);
        throw new IllegalStateException("Cannot resolve current user id from principal");
    }


    // 생성 (201 Created)
    @PostMapping
    public ResponseEntity<ApiResponse<Response>> create(@Valid @RequestBody CreateRequest req) {
        Response created = service.create(currentUserId(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "개인프로필 생성 완료"));
    }


    // 조회 (200 OK)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Response>> getMine() {
        Response me = service.getMine(currentUserId());
        return ResponseEntity.ok(ApiResponse.success(me, "개인프로필 조회 성공"));
    }


    // 수정 (200 OK)
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Response>> update(@Valid @RequestBody UpdateRequest req) {
        Response updated = service.update(currentUserId(), req);
        return ResponseEntity.ok(ApiResponse.success(updated, "개인프로필 수정 완료"));
    }


    // 삭제 (200 OK) — ApiResponse 래퍼 일관성 유지를 위해 204 대신 200 사용
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMine() {
        service.deleteMine(currentUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "개인프로필 삭제 완료"));
    }
}
