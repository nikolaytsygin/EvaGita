package com.eva.evagita.specification;

import com.eva.evagita.model.Task;
import com.eva.evagita.model.TaskPriority;
import com.eva.evagita.model.TaskStatus;
import com.eva.evagita.model.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class TaskSpecification {

    private TaskSpecification() {
    }

    public static Specification<Task> byFilters(
            TaskStatus status,
            TaskPriority priority,
            Long projectId,
            Long tagId,
            LocalDate dueDateFrom,
            LocalDate dueDateTo,
            User user
    ) {
        Specification<Task> specification = hasUser(user);

        if (status != null) {
            specification = specification.and(hasStatus(status));
        }

        if (priority != null) {
            specification = specification.and(hasPriority(priority));
        }

        if (projectId != null) {
            specification = specification.and(hasProject(projectId));
        }

        if (tagId != null) {
            specification = specification.and(hasTag(tagId));
        }

        if (dueDateFrom != null) {
            specification = specification.and(dueDateFrom(dueDateFrom));
        }

        if (dueDateTo != null) {
            specification = specification.and(dueDateTo(dueDateTo));
        }

        return specification;
    }

    private static Specification<Task> hasUser(User user) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user"), user);
    }

    private static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    private static Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("priority"), priority);
    }

    private static Specification<Task> hasProject(Long projectId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("project").get("id"),
                        projectId
                );
    }

    private static Specification<Task> hasTag(Long tagId) {
        return (root, query, criteriaBuilder) -> {
            Join<Task, ?> tags = root.join("tags", JoinType.INNER);

            query.distinct(true);

            return criteriaBuilder.equal(
                    tags.get("id"),
                    tagId
            );
        };
    }

    private static Specification<Task> dueDateFrom(LocalDate dueDateFrom) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("dueDate"),
                        dueDateFrom
                );
    }

    private static Specification<Task> dueDateTo(LocalDate dueDateTo) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("dueDate"),
                        dueDateTo
                );
    }
}
