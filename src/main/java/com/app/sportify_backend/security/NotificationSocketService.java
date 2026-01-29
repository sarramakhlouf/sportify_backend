package com.app.sportify_backend.security;

import com.app.sportify_backend.dto.NotificationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendToUser(String userEmail, NotificationResponse notif) {
        messagingTemplate.convertAndSendToUser(
                userEmail,
                "/queue/notifications",
                notif
        );
    }
}

