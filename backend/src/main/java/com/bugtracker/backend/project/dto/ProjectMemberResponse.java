package com.bugtracker.backend.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record ProjectMemberResponse(
        @JsonProperty("_id")
        UUID id,

        String email
) {}