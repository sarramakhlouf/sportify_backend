package com.app.sportify_backend.dto;

import com.app.sportify_backend.models.Invitation;
import com.app.sportify_backend.models.InvitationStatus;
import com.app.sportify_backend.models.InvitationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvitationResponse {
    private String id;

    private String senderId;
    private String senderName;
    private String receiverId;
    private String receiverName;

    private InvitationType type;
    private InvitationStatus status;

    //PLAYER_INVITATION
    private String teamId;
    private String teamName;

    //TEAM_MATCH_INVITATION
    private String senderTeamId;
    private String senderTeamName;
    private String receiverTeamId;
    private String receiverTeamName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InvitationResponse from(Invitation invitation) {
        InvitationResponseBuilder builder = InvitationResponse.builder()
                .id(invitation.getId())
                .senderId(invitation.getSenderId())
                .senderName(invitation.getSenderName())
                .receiverId(invitation.getReceiverId())
                .receiverName(invitation.getReceiverName())
                .type(invitation.getType())
                .status(invitation.getStatus())
                .createdAt(invitation.getCreatedAt())
                .updatedAt(invitation.getUpdatedAt());

        if (invitation.getType() == InvitationType.PLAYER_INVITATION) {
            builder.teamId(invitation.getTeamId())
                    .teamName(invitation.getTeamName());
        } else if (invitation.getType() == InvitationType.TEAM_MATCH_INVITATION) {
            builder.senderTeamId(invitation.getSenderTeamId())
                    .senderTeamName(invitation.getSenderTeamName())
                    .receiverTeamId(invitation.getReceiverTeamId())
                    .receiverTeamName(invitation.getReceiverTeamName());
        }

        return builder.build();
    }
}