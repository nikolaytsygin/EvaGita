package com.eva.evagita.service;

import com.eva.evagita.exception.ProjectNotFoundException;
import com.eva.evagita.model.Project;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    @Override
    public List<Project> getAllProjects(User user) {
        return projectRepository.findAllByUser(user);
    }

    @Override
    public Project getProjectById(Long id, User user) {
        return projectRepository.findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new ProjectNotFoundException(
                                "Project not found with id: " + id
                        ));
    }

    @Override
    public Project updateProject(Long id, Project project, User user) {
        Project existingProject = getProjectById(id, user);

        existingProject.setName(project.getName());
        existingProject.setDescription(project.getDescription());

        return projectRepository.save(existingProject);
    }

    @Override
    public void deleteProject(Long id, User user) {
        Project existingProject = getProjectById(id, user);
        projectRepository.delete(existingProject);
    }
}
