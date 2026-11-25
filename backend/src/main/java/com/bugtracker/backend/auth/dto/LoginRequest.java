package com.bugtracker.backend.auth.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String password
) {}