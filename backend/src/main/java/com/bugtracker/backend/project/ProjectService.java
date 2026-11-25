package com.bugtracker.backend.project;

import com.bugtracker.backend.project.dto.*;
import com.bugtracker.backend.user.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public Map<String, String> createProject(
            UUID userId, CreateProjectRequest request) {

        User owner = findUser(userId);

        Project project = new Project(
                request.name().trim(),
                request.description(),
                owner
        );

        projectRepository.save(project);

        return Map.of(
                "message",
                "Project " + project.getName() + " has been successfully created"
        );
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjects(UUID userId) {
        return projectRepository.findAccessibleProjects(userId)
                .stream()
                .map(project -> toResponse(project, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID projectId, UUID userId) {
        Project project = findProject(projectId);

        if (!hasAccess(project, userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You don't have permission to interact with this project"
            );
        }

        return toResponse(project, true);
    }

    public Map<String, ProjectResponse> updateProject(
            UUID projectId, UUID userId, UpdateProjectRequest request) {

        Project project = findProject(projectId);

        requireOwner(project, userId);

        if (request.name() != null) {
            String name = request.name().trim();

            if (name.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Project name cannot be empty"
                );
            }

            project.setName(name);
        }

        if (request.description() != null) {
            project.setDescription(request.description());
        }

        if (request.members() != null) {
            Set<User> members = new HashSet<>();

            for (String email : request.members()) {
                String normalizedEmail = email.trim()
                        .toLowerCase(Locale.ROOT);

                User member = userRepository.findByEmail(normalizedEmail)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "One or more member emails do not exist"
                        ));

                members.add(member);
            }

            project.setMembers(members);
        }

        Project updatedProject = projectRepository.save(project);

        return Map.of("project", toResponse(updatedProject, false));
    }

    public Map<String, String> deleteProject(UUID projectId, UUID userId) {
        Project project = findProject(projectId);

        requireOwner(project, userId);

        projectRepository.delete(project);

        return Map.of("message", "Project successfully deleted");
    }

    private Project findProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Project with this id doesn't exist"
                ));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"
                ));
    }

    private boolean isOwner(Project project, UUID userId) {
        return project.getOwner().getId().equals(userId);
    }

    private boolean hasAccess(Project project, UUID userId) {
        return isOwner(project, userId) || project.getMembers()
                .stream()
                .anyMatch(member -> member.getId().equals(userId));
    }

    private void requireOwner(Project project, UUID userId) {
        if (!isOwner(project, userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You don't have permission to modify this project"
            );
        }
    }

    private ProjectResponse toResponse(Project project, boolean populateMembers) {
        List<?> members;

        if (populateMembers) {
            members = project.getMembers()
                    .stream()
                    .map(member -> new ProjectMemberResponse(
                            member.getId(),
                            member.getEmail()
                    ))
                    .toList();
        } else {
            members = project.getMembers()
                    .stream()
                    .map(User::getId)
                    .toList();
        }

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwner().getId(),
                members
        );
    }
}