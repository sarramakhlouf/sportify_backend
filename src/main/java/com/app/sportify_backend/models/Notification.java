package com.app.sportify_backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "notifications")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @Id
    private String id;

    private String recipientId;
    private String senderId;

    private NotificationType type;
    private NotificationStatus status;
    private String title;
    private String message;

    private Map<String, Object> data;

    private boolean isRead;

    private String referenceId; // teamId / matchId / userId

    private LocalDateTime createdAt;
}