package com.app.sportify_backend.services;

import com.app.sportify_backend.models.Notification;
import com.app.sportify_backend.models.NotificationType;
import com.app.sportify_backend.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void send(
            String userId,
            String title,
            String message,
            NotificationType type,
            String referenceId
    ) {
        notificationRepository.save(
                Notification.builder()
                        .userId(userId)
                        .title(title)
                        .message(message)
                        .type(type)
                        .referenceId(referenceId)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }
}

