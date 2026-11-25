package com.bugtracker.backend.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record ProjectResponse(
        @JsonProperty("_id")
        UUID id,

        String name,
        String description,
        UUID owner,
        List<?> members
) {}