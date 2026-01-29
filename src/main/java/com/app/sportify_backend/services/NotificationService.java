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

    /**
     * Créer et envoyer une notification avec données additionnelles
     */
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

    /**
     * Récupérer toutes les notifications d'un utilisateur
     */
    public List<NotificationResponse> getUserNotifications(String userId) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les notifications non lues
     */
    public List<NotificationResponse> getUnreadNotifications(String userId) {
        return notificationRepository
                .findByRecipientIdAndStatus(userId, NotificationStatus.UNREAD)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Compter les notifications non lues
     */
    public long countUnread(String userId) {
        return notificationRepository.countByRecipientIdAndStatus(
                userId,
                NotificationStatus.UNREAD
        );
    }

    /**
     * Marquer une notification comme lue
     */
    public void markAsRead(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("NOTIFICATION_NOT_FOUND"));

        if (!notification.getRecipientId().equals(userId)) {
            throw new RuntimeException("NOT_ALLOWED");
        }

        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);
    }

    /**
     * Marquer toutes les notifications comme lues
     */
    public void markAllAsRead(String userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientIdAndStatus(userId, NotificationStatus.UNREAD);

        unreadNotifications.forEach(n -> n.setStatus(NotificationStatus.READ));
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Supprimer une notification
     */
    public void deleteNotification(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("NOTIFICATION_NOT_FOUND"));

        if (!notification.getRecipientId().equals(userId)) {
            throw new RuntimeException("NOT_ALLOWED");
        }

        notificationRepository.delete(notification);
    }

    /**
     * Convertir Notification en NotificationResponse
     */
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