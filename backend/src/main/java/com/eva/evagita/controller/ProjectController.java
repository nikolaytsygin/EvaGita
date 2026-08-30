package com.eva.evagita.controller;

import com.eva.evagita.dto.project.ProjectRequest;
import com.eva.evagita.dto.project.ProjectResponse;
import com.eva.evagita.model.Project;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;

    public ProjectController(
            ProjectService projectService,
            UserRepository userRepository
    ) {
        this.projectService = projectService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<ProjectResponse> getAllProjects() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = getCurrentUser(authentication);

        return projectService.getAllProjects(currentUser)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ProjectResponse getProjectById(@PathVariable Long id) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = getCurrentUser(authentication);

        return toResponse(projectService.getProjectById(id, currentUser));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(
            @Valid @RequestBody ProjectRequest request
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = getCurrentUser(authentication);

        Project project = toEntity(request, currentUser);

        return toResponse(projectService.createProject(project));
    }

    @PutMapping("/{id}")
    public ProjectResponse updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = getCurrentUser(authentication);

        Project project = toEntity(request, currentUser);

        return toResponse(
                projectService.updateProject(id, project, currentUser)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable Long id) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = getCurrentUser(authentication);

        projectService.deleteProject(id, currentUser);
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }

    private Project toEntity(ProjectRequest request, User user) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setUser(user);
        return project;
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
