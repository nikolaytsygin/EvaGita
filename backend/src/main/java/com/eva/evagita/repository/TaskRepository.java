package com.eva.evagita.repository;

import com.eva.evagita.model.Task;
import com.eva.evagita.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByUser(User user);

    @EntityGraph(attributePaths = "tags")
    Optional<Task> findByIdAndUser(Long id, User user);
}
