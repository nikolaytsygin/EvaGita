package com.eva.evagita.service;

import com.eva.evagita.exception.UserNotFoundException;
import com.eva.evagita.model.Notification;
import com.eva.evagita.model.NotificationType;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository
    ) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<Notification> getNotifications(User user) {
        return notificationRepository
                .findAllByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository
                .findAllByUserAndReadFalseOrderByCreatedAtDesc(user);
    }

    @Override
    public long countUnreadNotifications(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Override
    public Notification createNotification(
            User user,
            NotificationType type,
            String message
    ) {
        Notification notification = new Notification();

        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);

        return notificationRepository.save(notification);
    }

    @Override
    public Notification markAsRead(
            Long notificationId,
            User user
    ) {
        Notification notification = notificationRepository
                .findByIdAndUser(notificationId, user)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Notification not found: " + notificationId
                        )
                );

        notification.setRead(true);

        return notificationRepository.save(notification);
    }
}
