package com.app.sportify_backend.services;

import com.app.sportify_backend.models.*;
import com.app.sportify_backend.repositories.InvitationRepository;
import com.app.sportify_backend.repositories.TeamMemberRepository;
import com.app.sportify_backend.repositories.TeamRepository;
import com.app.sportify_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvitationService {
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final InvitationRepository invitationRepository;

    public Invitation invitePlayer(String teamId, String senderId, String playerCode) {

        User receiver = userRepository.findByPlayerCode(playerCode)
                .orElseThrow(() -> new RuntimeException("PLAYER_NOT_FOUND"));

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, receiver.getId())) {
            throw new RuntimeException("ALREADY_MEMBER");
        }

        return invitationRepository.save(
                Invitation.builder()
                        .teamId(teamId)
                        .senderId(senderId)
                        .receiverId(receiver.getId())
                        .status(InvitationStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    public void acceptInvitation(String invitationId, String userId) {

        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("INVITATION_NOT_FOUND"));

        if (!invitation.getReceiverId().equals(userId)) {
            throw new RuntimeException("NOT_ALLOWED");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);

        teamMemberRepository.save(
                TeamMember.builder()
                        .teamId(invitation.getTeamId())
                        .userId(userId)
                        .role(MemberRole.MEMBER)
                        .build()
        );
    }

    public void refuseInvitation(String invitationId, String userId) {

        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("INVITATION_NOT_FOUND"));

        if (!invitation.getReceiverId().equals(userId)) {
            throw new RuntimeException("NOT_ALLOWED");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("INVITATION_ALREADY_PROCESSED");
        }

        invitation.setStatus(InvitationStatus.REJECTED);
        invitationRepository.save(invitation);
    }
}
