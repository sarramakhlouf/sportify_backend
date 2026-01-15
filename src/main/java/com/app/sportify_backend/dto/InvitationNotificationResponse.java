package com.app.sportify_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvitationNotificationResponse {

    private String notificationId;
    private String invitationId;

    private String teamId;
    private String teamName;
    private String teamCity;
    private String teamLogo;

    private String title;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
}

