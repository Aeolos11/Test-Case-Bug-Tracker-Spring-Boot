package com.bugtracker.backend.project;

import com.bugtracker.backend.project.dto.*;
import com.bugtracker.backend.user.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    private UUID ownerId;
    private UUID memberId;
    private UUID outsiderId;
    private UUID projectId;

    private User owner;
    private User member;
    private Project project;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        outsiderId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        owner = mock(User.class);
        member = mock(User.class);

        project = new Project(
                "Test Project",
                "Test description",
                owner
        );
    }

    @Test
    void createProjectSuccessfully() {
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        var request = new CreateProjectRequest(
                "New Project",
                "New description"
        );

        var result = projectService.createProject(
                ownerId, request
        );

        assertTrue(result.get("message")
                .contains("New Project"));

        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProjectWithUnknownUser() {
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.empty());

        var request = new CreateProjectRequest(
                "New Project",
                "Description"
        );

        var exception = assertThrows(
                ResponseStatusException.class,
                () -> projectService.createProject(ownerId, request)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verify(projectRepository, never()).save(any());
    }

    @Test
    void getAccessibleProjects() {
        when(owner.getId()).thenReturn(ownerId);

        when(projectRepository.findAccessibleProjects(ownerId))
                .thenReturn(List.of(project));

        var result = projectService.getProjects(ownerId);

        assertEquals(1, result.size());
        assertEquals("Test Project", result.get(0).name());
    }

    @Test
    void ownerCanViewProject() {
        when(owner.getId()).thenReturn(ownerId);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        var result = projectService.getProjectById(
                projectId, ownerId
        );

        assertEquals("Test Project", result.name());
        assertEquals(ownerId, result.owner());
    }

    @Test
    void memberCanViewProject() {
        when(owner.getId()).thenReturn(ownerId);
        when(member.getId()).thenReturn(memberId);

        project.getMembers().add(member);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        var result = projectService.getProjectById(
                projectId, memberId
        );

        assertEquals("Test Project", result.name());
        assertEquals(1, result.members().size());
    }

    @Test
    void outsiderCannotViewProject() {
        when(owner.getId()).thenReturn(ownerId);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        var exception = assertThrows(
                ResponseStatusException.class,
                () -> projectService.getProjectById(
                        projectId, outsiderId
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );
    }

    @Test
    void getNonExistingProject() {
        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        var exception = assertThrows(
                ResponseStatusException.class,
                () -> projectService.getProjectById(
                        projectId, ownerId
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void ownerCanUpdateProject() {
        when(owner.getId()).thenReturn(ownerId);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectRepository.save(project))
                .thenReturn(project);

        var request = new UpdateProjectRequest(
                "Updated Project",
                "Updated description",
                null
        );

        var result = projectService.updateProject(
                projectId, ownerId, request
        );

        assertEquals(
                "Updated Project",
                result.get("project").name()
        );

        assertEquals(
                "Updated description",
                project.getDescription()
        );

        verify(projectRepository).save(project);
    }

    @Test
    void memberCannotUpdateProject() {
        when(owner.getId()).thenReturn(ownerId);

        project.getMembers().add(member);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        var request = new UpdateProjectRequest(
                "Unauthorized update",
                null,
                null
        );

        var exception = assertThrows(
                ResponseStatusException.class,
                () -> projectService.updateProject(
                        projectId, memberId, request
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );

        verify(projectRepository, never()).save(any());
    }

    @Test
    void ownerCanAddMember() {
        when(owner.getId()).thenReturn(ownerId);
        when(member.getId()).thenReturn(memberId);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(userRepository.findByEmail("member@example.com"))
                .thenReturn(Optional.of(member));

        when(projectRepository.save(project))
                .thenReturn(project);

        var request = new UpdateProjectRequest(
                null,
                null,
                List.of("member@example.com")
        );

        var result = projectService.updateProject(
                projectId, ownerId, request
        );

        assertTrue(project.getMembers().contains(member));

        assertEquals(
                List.of(memberId),
                result.get("project").members()
        );
    }

    @Test
    void cannotAddUnknownMember() {
        when(owner.getId()).thenReturn(ownerId);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        var request = new UpdateProjectRequest(
                null,
                null,
                List.of("unknown@example.com")
        );

        var exception = assertThrows(
                ResponseStatusException.class,
                () -> projectService.updateProject(
                        projectId, ownerId, request
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(projectRepository, never()).save(any());
    }

    @Test
    void ownerCanDeleteProject() {
        when(owner.getId()).thenReturn(ownerId);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        projectService.deleteProject(projectId, ownerId);

        verify(projectRepository).delete(project);
    }

    @Test
    void memberCannotDeleteProject() {
        when(owner.getId()).thenReturn(ownerId);

        project.getMembers().add(member);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        var exception = assertThrows(
                ResponseStatusException.class,
                () -> projectService.deleteProject(
                        projectId, memberId
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );

        verify(projectRepository, never()).delete(any());
    }
}