package com.app.sportify_backend.services;

import com.app.sportify_backend.dto.InvitationNotificationResponse;
import com.app.sportify_backend.models.Invitation;
import com.app.sportify_backend.models.Notification;
import com.app.sportify_backend.models.NotificationType;
import com.app.sportify_backend.models.Team;
import com.app.sportify_backend.repositories.InvitationRepository;
import com.app.sportify_backend.repositories.NotificationRepository;
import com.app.sportify_backend.repositories.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final InvitationRepository invitationRepository;
    private final TeamRepository teamRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void send(
            String userId,
            String title,
            String message,
            NotificationType type,
            String referenceId
    ) {
        Notification notification = notificationRepository.save(
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

        // 🔴 PUSH temps réel
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notifications",
                notification
        );
    }

    public List<InvitationNotificationResponse> getInvitationNotifications(String userId) {

        List<Notification> notifications =
                notificationRepository.findByUserIdAndType(
                        userId,
                        NotificationType.INVITATION_RECEIVED
                );

        return notifications.stream().map(notification -> {

            Invitation invitation =
                    invitationRepository.findById(notification.getReferenceId())
                            .orElseThrow();

            Team team =
                    teamRepository.findById(invitation.getTeamId())
                            .orElseThrow();

            return InvitationNotificationResponse.builder()
                    .notificationId(notification.getId())
                    .invitationId(invitation.getId())
                    .teamId(team.getId())
                    .teamName(team.getName())
                    .teamCity(team.getCity())
                    .teamLogo(team.getLogoUrl())
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                    .isRead(notification.isRead())
                    .createdAt(notification.getCreatedAt())
                    .build();

        }).toList();
    }

}


