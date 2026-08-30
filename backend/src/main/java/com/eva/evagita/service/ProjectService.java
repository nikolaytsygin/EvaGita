package com.eva.evagita.service;

import com.eva.evagita.model.Project;
import com.eva.evagita.model.User;

import java.util.List;

public interface ProjectService {

    Project createProject(Project project);

    List<Project> getAllProjects(User user);

    Project getProjectById(Long id, User user);

    Project updateProject(Long id, Project project, User user);

    void deleteProject(Long id, User user);
}
