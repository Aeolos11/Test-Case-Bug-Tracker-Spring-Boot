package com.bugtracker.backend.auth.dto;

import java.util.UUID;

public record UserResponse(
        UUID _id,
        String name,
        String email,
        String role
) {}