package com.eva.evagita.repository;

import com.eva.evagita.model.Notification;
import com.eva.evagita.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByIdAndUser(Long id, User user);

    List<Notification> findAllByUserOrderByCreatedAtDesc(User user);

    List<Notification> findAllByUserAndReadFalseOrderByCreatedAtDesc(User user);

    long countByUserAndReadFalse(User user);
}
