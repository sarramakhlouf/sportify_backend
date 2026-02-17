package com.app.sportify_backend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "invitations")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@CompoundIndex(
        name = "invitation_unique",
        def = "{'senderTeamId': 1, 'receiverTeamId': 1}",
        unique = true )
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
    private boolean cancelledBySender = false;

    @Builder.Default
    private boolean cancelledByReceiver = false;

    private CancelReason cancelReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}