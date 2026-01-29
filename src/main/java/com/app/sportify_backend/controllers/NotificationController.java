package com.app.sportify_backend.controllers;

import com.app.sportify_backend.dto.NotificationResponse;
import com.app.sportify_backend.models.User;
import com.app.sportify_backend.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<NotificationResponse> getMyNotifications(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return notificationService.getUserNotifications(user.getId());
    }

    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public List<NotificationResponse> getUnreadNotifications(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return notificationService.getUnreadNotifications(user.getId());
    }

    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Long> countUnread(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        long count = notificationService.countUnread(user.getId());
        return Map.of("count", count);
    }

    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public void markAsRead(
            @PathVariable String id,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        notificationService.markAsRead(id, user.getId());
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public void markAllAsRead(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        notificationService.markAllAsRead(user.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void deleteNotification(
            @PathVariable String id,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        notificationService.deleteNotification(id, user.getId());
    }
}