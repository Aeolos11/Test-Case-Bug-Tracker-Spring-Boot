package com.bugtracker.backend.project;

import com.bugtracker.backend.project.dto.*;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/create-project")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> createProject(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateProjectRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());

        return projectService.createProject(userId, request);
    }

    @GetMapping("/projects")
    public List<ProjectResponse> getProjects(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        return projectService.getProjects(userId);
    }

    @GetMapping("/projects/{id}")
    public ProjectResponse getProjectById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        return projectService.getProjectById(id, userId);
    }

    @PutMapping("/projects/{id}")
    public Map<String, ProjectResponse> updateProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProjectRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());

        return projectService.updateProject(id, userId, request);
    }

    @DeleteMapping("/projects/{id}")
    public Map<String, String> deleteProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        return projectService.deleteProject(id, userId);
    }
}