package com.eva.evagita.repository;

import com.eva.evagita.model.Task;
import com.eva.evagita.model.TaskStatus;
import com.eva.evagita.model.TaskPriority;
import com.eva.evagita.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    @EntityGraph(attributePaths = {"tags", "project"})
    List<Task> findAllByUser(User user);

    @EntityGraph(attributePaths = "tags")
    List<Task> findAllByUserAndProject_Id(User user, Long projectId);

    @EntityGraph(attributePaths = "tags")
    List<Task> findAllByUserAndTags_Id(User user, Long tagId);

    @EntityGraph(attributePaths = "tags")
    List<Task> findAllByUserAndStatus(User user, TaskStatus status);

    @EntityGraph(attributePaths = "tags")
    List<Task> findAllByUserAndPriority(User user, TaskPriority priority);

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

    long countByUser(User user);

    long countByUserAndStatus(User user, TaskStatus status);

    long countByUserAndPriority(User user, TaskPriority priority);

    long countByUserAndDueDateBeforeAndStatusNot(
            User user,
            LocalDate dueDate,
            TaskStatus status
    );

    @EntityGraph(attributePaths = {"tags", "project"})
    List<Task> findAllByDueDateBeforeAndStatusNot(
            LocalDate dueDate,
            TaskStatus status
    );
}
