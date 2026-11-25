package com.bugtracker.backend.project.dto;

import jakarta.validation.constraints.*;

public record CreateProjectRequest(
        @NotBlank
        @Size(max = 255)
        String name,

        String description
) {}