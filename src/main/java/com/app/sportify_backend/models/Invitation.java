package com.app.sportify_backend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "invitations")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Invitation {

    @Id
    private String id;

    private String teamId;
    private String senderId;
    private String receiverId;

    private InvitationStatus status;

    private LocalDateTime createdAt;
}
