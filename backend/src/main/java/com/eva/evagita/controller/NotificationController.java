package com.eva.evagita.controller;

import com.eva.evagita.dto.NotificationResponse;
import com.eva.evagita.exception.UserNotFoundException;
import com.eva.evagita.model.Notification;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(
            NotificationService notificationService,
            UserRepository userRepository
    ) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<NotificationResponse> getNotifications() {
        User currentUser = getCurrentUser();

        return notificationService
                .getNotifications(currentUser)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @GetMapping("/unread")
    public List<NotificationResponse> getUnreadNotifications() {
        User currentUser = getCurrentUser();

        return notificationService
                .getUnreadNotifications(currentUser)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @GetMapping("/unread/count")
    public long countUnreadNotifications() {
        User currentUser = getCurrentUser();

        return notificationService.countUnreadNotifications(currentUser);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(
            @PathVariable Long id
    ) {
        User currentUser = getCurrentUser();

        Notification notification =
                notificationService.markAsRead(id, currentUser);

        return NotificationResponse.from(notification);
    }

    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException(authentication.getName()));
    }
}
