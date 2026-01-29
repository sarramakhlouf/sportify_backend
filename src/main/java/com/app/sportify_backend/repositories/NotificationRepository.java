package com.app.sportify_backend.repositories;

import com.app.sportify_backend.models.Notification;
import com.app.sportify_backend.models.NotificationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);

    List<Notification> findByRecipientIdAndStatus(String recipientId, NotificationStatus status);

    long countByRecipientIdAndStatus(String recipientId, NotificationStatus status);
}
