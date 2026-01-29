package com.app.sportify_backend.dto;

import com.app.sportify_backend.models.NotificationStatus;
import com.app.sportify_backend.models.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private String id;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationStatus status;
    private String referenceId;
    private Map<String, Object> data;
    private LocalDateTime createdAt;
}