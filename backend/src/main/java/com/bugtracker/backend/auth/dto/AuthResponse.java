package com.bugtracker.backend.auth.dto;
import com.bugtracker.backend.auth.dto.*;
public record AuthResponse(
        String token,
        UserResponse user
) {}