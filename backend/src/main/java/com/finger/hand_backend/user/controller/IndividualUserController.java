package com.finger.hand_backend.user.controller;


import com.finger.hand_backend.user.dto.IndividualUserDtos.*;
import com.finger.hand_backend.user.service.IndividualUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/individual-users")
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


    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody CreateRequest req) {
        return ResponseEntity.ok(service.create(currentUserId(), req));
    }


    @GetMapping("/me")
    public ResponseEntity<Response> getMine() {
        return ResponseEntity.ok(service.getMine(currentUserId()));
    }


    @PutMapping("/me")
    public ResponseEntity<Response> update(@Valid @RequestBody UpdateRequest req) {
        return ResponseEntity.ok(service.update(currentUserId(), req));
    }


    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMine() {
        service.deleteMine(currentUserId());
        return ResponseEntity.noContent().build();
    }
}
