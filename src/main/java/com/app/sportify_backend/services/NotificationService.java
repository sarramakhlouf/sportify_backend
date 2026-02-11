package com.app.sportify_backend.services;

import com.app.sportify_backend.dto.NotificationResponse;
import com.app.sportify_backend.models.*;
import com.app.sportify_backend.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

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

        notification = notificationRepository.save(notification);

        messagingTemplate.convertAndSendToUser(
                recipientId,
                "/queue/notifications",
                toResponse(notification)
        );

        return notification;
    }

    public void sendInvitationCancelledNotification(Invitation invitation, String actorId, CancelReason reason, String message) {
        List<String> recipients = new ArrayList<>();

        if (invitation.getSenderId() != null) recipients.add(invitation.getSenderId());
        if (invitation.getReceiverId() != null) recipients.add(invitation.getReceiverId());

        for (String userId : recipients) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("invitationId", invitation.getId());
            payload.put("status", invitation.getStatus());
            payload.put("type", invitation.getType());
            payload.put("message", message);
            payload.put("cancelledBy", actorId);
            payload.put("teamId", invitation.getTeamId());

            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/invitations",
                    payload
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