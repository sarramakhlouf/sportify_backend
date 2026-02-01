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

    //COMMUN
    private String senderId;
    private String senderName;
    private String receiverId;
    private String receiverName;

    private InvitationType type;
    private InvitationStatus status;

    //PLAYER_INVITATION
    private String teamId;
    private String teamName;
    private String teamLogoUrl;

    //TEAM_MATCH_INVITATION
    private String senderTeamId;
    private String senderTeamName;
    private String senderTeamLogoUrl;
    private String receiverTeamId;
    private String receiverTeamName;
    private String receiverTeamLogoUrl;

    @Builder.Default
    private boolean deletedBySender = false;

    @Builder.Default
    private boolean deletedByReceiver = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}