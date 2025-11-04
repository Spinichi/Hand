package com.finger.hand_backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    @Email @NotBlank
    private String email;
    @NotBlank @Size(max = 255)
    private String password;
}

