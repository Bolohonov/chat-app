package com.chat.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 30) String username,
        @NotBlank String email,
        @NotBlank @Size(min = 6) String password,
        String displayName
) {}
