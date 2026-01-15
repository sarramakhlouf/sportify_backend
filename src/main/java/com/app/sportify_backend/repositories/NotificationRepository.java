package com.app.sportify_backend.repositories;

import com.app.sportify_backend.models.Notification;
import com.app.sportify_backend.models.NotificationType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    long countByUserIdAndIsReadFalse(String userId);
    List<Notification> findByUserIdAndType(String userId, NotificationType type);
}
