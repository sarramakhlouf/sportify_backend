package com.app.sportify_backend.services;

import com.app.sportify_backend.models.*;
import com.app.sportify_backend.repositories.InvitationRepository;
import com.app.sportify_backend.repositories.TeamRepository;
import com.app.sportify_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final InvitationRepository invitationRepository;
    private final NotificationService notificationService;

    // ---------------- Invite player ----------------
    public Invitation invitePlayer(String teamId, String senderId, String playerCode) {

        User receiver = userRepository.findByPlayerCode(playerCode)
                .orElseThrow(() -> new RuntimeException("PLAYER_NOT_FOUND"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("TEAM_NOT_FOUND"));

        boolean isMember = team.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(receiver.getId()));
        if (isMember) {
            throw new RuntimeException("ALREADY_MEMBER");
        }

        Invitation invitation = invitationRepository.save(
                Invitation.builder()
                        .teamId(teamId)
                        .senderId(senderId)
                        .receiverId(receiver.getId())
                        .status(InvitationStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        notificationService.send(
                receiver.getId(),
                "Invitation d’équipe",
                "Vous avez reçu une invitation pour rejoindre l'équipe " + team.getName(),
                NotificationType.INVITATION_RECEIVED,
                invitation.getId()
        );

        return invitation;
    }

    // ---------------- Accept invitation ----------------
    public void acceptInvitation(String invitationId, String userId) {

        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("INVITATION_NOT_FOUND"));

        if (!invitation.getReceiverId().equals(userId)) {
            throw new RuntimeException("NOT_ALLOWED");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);

        Team team = teamRepository.findById(invitation.getTeamId())
                .orElseThrow(() -> new RuntimeException("TEAM_NOT_FOUND"));

        team.getMembers().add(
                Team.TeamMember.builder()
                        .userId(userId)
                        .role(MemberRole.MEMBER)
                        .build()
        );
        teamRepository.save(team);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        user.getTeamIds().add(team.getId());
        userRepository.save(user);

        notificationService.send(
                invitation.getSenderId(),
                "Invitation acceptée",
                "Votre invitation a été acceptée par " + user.getFirstname(),
                NotificationType.INVITATION_ACCEPTED,
                invitation.getId()
        );
    }

    // ---------------- Refuse invitation ----------------
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

        notificationService.send(
                invitation.getSenderId(),
                "Invitation refusée",
                "Votre invitation a été refusée par " + userRepository.findById(userId).map(User::getFirstname).orElse("un joueur"),
                NotificationType.INVITATION_REJECTED,
                invitation.getId()
        );
    }

    // ---------------- Get pending invitations ----------------
    public List<Invitation> getPendingInvitations(String userId) {
        return invitationRepository
                .findByReceiverIdAndStatus(userId, InvitationStatus.PENDING);
    }
}
