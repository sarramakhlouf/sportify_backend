package com.app.sportify_backend.controllers;

import com.app.sportify_backend.models.Notification;
import com.app.sportify_backend.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository repository;

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
}

