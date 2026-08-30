package com.eva.evagita.repository;

import com.eva.evagita.model.Project;
import com.eva.evagita.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByUser(User user);

    Optional<Project> findByIdAndUser(Long id, User user);
}
