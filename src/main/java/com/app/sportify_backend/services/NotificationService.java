package com.app.sportify_backend.services;

import com.app.sportify_backend.dto.NotificationResponse;
import com.app.sportify_backend.models.*;
import com.app.sportify_backend.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Créer et envoyer une notification
     */
    public Notification send(
            String recipientId,
            String senderId,
            String title,
            String message,
            NotificationType type,
            String referenceId
    ) {
        return send(recipientId, senderId, title, message, type, referenceId, null);
    }


    public Notification send(
            String recipientId,
            String senderId,
            String title,
            String message,
            NotificationType type,
            String referenceId,
            Map<String, Object> data
    ) {
        Notification notification = Notification.builder()
                .recipientId(recipientId)
                .senderId(senderId)
                .title(title)
                .message(message)
                .type(type)
                .status(NotificationStatus.UNREAD)
                .referenceId(referenceId)
                .data(data)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    public void sendInvitationCancelledNotification(
            Invitation invitation,
            String cancelledByUserId,
            CancelReason reason,
            String customMessage
    ) {

        boolean cancelledBySender = invitation.getSenderId().equals(cancelledByUserId);

        String reasonText = switch (reason) {
            case NO_PLAYERS -> "manque de joueurs";
            case PITCH_NOT_AVAILABLE -> "terrain non disponible";
            case BAD_WEATHER -> "mauvaises conditions météo";
            case OTHERS -> customMessage != null ? customMessage : "autre raison";
        };

        Map<String, Object> data = new HashMap<>();
        data.put("invitationId", invitation.getId());
        data.put("reason", reason.name());
        data.put("reasonText", reasonText);

        if (cancelledBySender) {
            send(
                    invitation.getReceiverId(),
                    cancelledByUserId,
                    "Invitation annulée",
                    "L'invitation de l'équipe " + invitation.getSenderTeamName()
                            + " a été annulée à cause de " + reasonText,
                    NotificationType.MATCH_CANCELLED,
                    invitation.getId(),
                    data
            );
        } else {
            send(
                    invitation.getSenderId(),
                    cancelledByUserId,
                    "Invitation annulée",
                    "L'équipe " + invitation.getReceiverTeamName()
                            + " a annulé l'invitation à cause de " + reasonText,
                    NotificationType.MATCH_CANCELLED,
                    invitation.getId(),
                    data
            );
        }
    }

    public List<NotificationResponse> getUserNotifications(String userId) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    public List<NotificationResponse> getUnreadNotifications(String userId) {
        return notificationRepository
                .findByRecipientIdAndStatus(userId, NotificationStatus.UNREAD)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public long countUnread(String userId) {
        return notificationRepository.countByRecipientIdAndStatus(
                userId,
                NotificationStatus.UNREAD
        );
    }


    public void markAsRead(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("NOTIFICATION_NOT_FOUND"));

        if (!notification.getRecipientId().equals(userId)) {
            throw new RuntimeException("NOT_ALLOWED");
        }

        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(String userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientIdAndStatus(userId, NotificationStatus.UNREAD);

        unreadNotifications.forEach(n -> n.setStatus(NotificationStatus.READ));
        notificationRepository.saveAll(unreadNotifications);
    }


    public void deleteNotification(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("NOTIFICATION_NOT_FOUND"));

        if (!notification.getRecipientId().equals(userId)) {
            throw new RuntimeException("NOT_ALLOWED");
        }

        notificationRepository.delete(notification);
    }


    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .status(notification.getStatus())
                .referenceId(notification.getReferenceId())
                .data(notification.getData())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}