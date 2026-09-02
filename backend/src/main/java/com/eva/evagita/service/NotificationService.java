package com.eva.evagita.service;

import com.eva.evagita.model.Notification;
import com.eva.evagita.model.User;

import java.util.List;

public interface NotificationService {

    List<Notification> getNotifications(User user);

    List<Notification> getUnreadNotifications(User user);

    long countUnreadNotifications(User user);

    Notification createNotification(
            User user,
            com.eva.evagita.model.NotificationType type,
            String message
    );

    Notification markAsRead(Long notificationId, User user);
}
