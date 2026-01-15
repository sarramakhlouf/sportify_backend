package com.app.sportify_backend.controllers;

import com.app.sportify_backend.dto.InvitationNotificationResponse;
import com.app.sportify_backend.models.Notification;
import com.app.sportify_backend.models.User;
import com.app.sportify_backend.repositories.NotificationRepository;
import com.app.sportify_backend.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository repository;
    private final NotificationService notificationService;

    @GetMapping("/{userId}")
    public List<Notification> getUserNotifications(@PathVariable String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable String id) {
        Notification notif = repository.findById(id).orElseThrow();
        notif.setRead(true);
        repository.save(notif);
    }

    @GetMapping("/notifications/invitations")
    public ResponseEntity<List<InvitationNotificationResponse>> getInvitations(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(
                notificationService.getInvitationNotifications(user.getId())
        );
    }

}

