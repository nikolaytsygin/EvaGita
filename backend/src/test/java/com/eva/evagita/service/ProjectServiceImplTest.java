package com.eva.evagita.service;

import com.eva.evagita.exception.ProjectNotFoundException;
import com.eva.evagita.model.Project;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User user;
    private Project project;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");

        project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        project.setDescription("Test description");
        project.setUser(user);
    }

    @Test
    void createProject_shouldSaveAndReturnProject() {
        when(projectRepository.save(project)).thenReturn(project);

        Project result = projectService.createProject(project);

        assertNotNull(result);
        assertEquals(project, result);

        verify(projectRepository).save(project);
    }

    @Test
    void getAllProjects_shouldReturnUserProjects() {
        List<Project> projects = List.of(project);

        when(projectRepository.findAllByUser(user)).thenReturn(projects);

        List<Project> result = projectService.getAllProjects(user);

        assertEquals(1, result.size());
        assertEquals(project, result.get(0));

        verify(projectRepository).findAllByUser(user);
    }

    @Test
    void getProjectById_shouldReturnProjectWhenFound() {
        when(projectRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(project));

        Project result = projectService.getProjectById(1L, user);

        assertNotNull(result);
        assertEquals(project, result);

        verify(projectRepository).findByIdAndUser(1L, user);
    }

    @Test
    void getProjectById_shouldThrowExceptionWhenNotFound() {
        when(projectRepository.findByIdAndUser(999L, user))
                .thenReturn(Optional.empty());

        ProjectNotFoundException exception = assertThrows(
                ProjectNotFoundException.class,
                () -> projectService.getProjectById(999L, user)
        );

        assertEquals(
                "Project not found with id: 999",
                exception.getMessage()
        );

        verify(projectRepository).findByIdAndUser(999L, user);
    }

    @Test
    void updateProject_shouldUpdateAndSaveProject() {
        Project updatedProject = new Project();
        updatedProject.setName("Updated Project");
        updatedProject.setDescription("Updated description");
        updatedProject.setUser(user);

        when(projectRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(project));
        when(projectRepository.save(project))
                .thenReturn(project);

        Project result = projectService.updateProject(
                1L,
                updatedProject,
                user
        );

        assertEquals("Updated Project", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(user, result.getUser());

        verify(projectRepository).findByIdAndUser(1L, user);
        verify(projectRepository).save(project);
    }

    @Test
    void updateProject_shouldThrowExceptionWhenProjectNotFound() {
        Project updatedProject = new Project();
        updatedProject.setName("Updated Project");
        updatedProject.setDescription("Updated description");

        when(projectRepository.findByIdAndUser(999L, user))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class,
                () -> projectService.updateProject(
                        999L,
                        updatedProject,
                        user
                )
        );

        verify(projectRepository).findByIdAndUser(999L, user);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void deleteProject_shouldDeleteProject() {
        when(projectRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(project));

        projectService.deleteProject(1L, user);

        verify(projectRepository).findByIdAndUser(1L, user);
        verify(projectRepository).delete(project);
    }

    @Test
    void deleteProject_shouldThrowExceptionWhenProjectNotFound() {
        when(projectRepository.findByIdAndUser(999L, user))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class,
                () -> projectService.deleteProject(999L, user)
        );

        verify(projectRepository).findByIdAndUser(999L, user);
        verify(projectRepository, never()).delete(any(Project.class));
    }


    @Test
    void getProjectById_shouldNotAllowAccessToAnotherUsersProject() {
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");

        when(projectRepository.findByIdAndUser(1L, anotherUser))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class,
                () -> projectService.getProjectById(1L, anotherUser)
        );

        verify(projectRepository).findByIdAndUser(1L, anotherUser);
    }

    @Test
    void updateProject_shouldNotAllowUpdatingAnotherUsersProject() {
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");

        Project updatedProject = new Project();
        updatedProject.setName("Hacked Project");
        updatedProject.setDescription("Hacked description");

        when(projectRepository.findByIdAndUser(1L, anotherUser))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class,
                () -> projectService.updateProject(
                        1L,
                        updatedProject,
                        anotherUser
                )
        );

        verify(projectRepository).findByIdAndUser(1L, anotherUser);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void deleteProject_shouldNotAllowDeletingAnotherUsersProject() {
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");

        when(projectRepository.findByIdAndUser(1L, anotherUser))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class,
                () -> projectService.deleteProject(1L, anotherUser)
        );

        verify(projectRepository).findByIdAndUser(1L, anotherUser);
        verify(projectRepository, never()).delete(any(Project.class));
    }

}
