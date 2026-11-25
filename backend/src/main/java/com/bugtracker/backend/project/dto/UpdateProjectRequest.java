package com.bugtracker.backend.project.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record UpdateProjectRequest(
        @Size(min = 1, max = 255)
        String name,

        String description,

        List<@NotBlank @Email String> members
) {}