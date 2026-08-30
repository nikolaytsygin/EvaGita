package com.eva.evagita.repository;

import com.eva.evagita.model.Task;
import com.eva.evagita.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByUser(User user);

    @EntityGraph(attributePaths = "tags")
    List<Task> findAllByUserAndDueDateGreaterThanEqual(
            User user,
            LocalDate dueDateFrom
    );

    @EntityGraph(attributePaths = "tags")
    List<Task> findAllByUserAndDueDateLessThanEqual(
            User user,
            LocalDate dueDateTo
    );

    @EntityGraph(attributePaths = "tags")
    List<Task> findAllByUserAndDueDateBetween(
            User user,
            LocalDate dueDateFrom,
            LocalDate dueDateTo
    );

    @EntityGraph(attributePaths = "tags")
    List<Task> findAllByUserAndTitleContainingIgnoreCase(User user, String title);

    @EntityGraph(attributePaths = "tags")
    List<Task> findAllByUserAndDescriptionContainingIgnoreCase(User user, String description);

    @EntityGraph(attributePaths = "tags")
    Optional<Task> findByIdAndUser(Long id, User user);
}
