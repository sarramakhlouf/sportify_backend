package com.app.sportify_backend.services;

import com.app.sportify_backend.dto.InvitationResponse;
import com.app.sportify_backend.models.*;
import com.app.sportify_backend.repositories.InvitationRepository;
import com.app.sportify_backend.repositories.TeamRepository;
import com.app.sportify_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final InvitationRepository invitationRepository;
    private final NotificationService notificationService;

    //---------------------INVITE PLAYER--------------------------------------------------------------------------------
    @Transactional
    public InvitationResponse invitePlayer(String teamId, String senderId, String playerCode) {

        User receiver = userRepository.findByPlayerCode(playerCode)
                .orElseThrow(() -> new RuntimeException("PLAYER_NOT_FOUND"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("TEAM_NOT_FOUND"));

        boolean isOwner =
                team.getOwnerId().equals(senderId) ||
                        team.getMembers().stream().anyMatch(m ->
                                m.getUserId().equals(senderId) &&
                                        (m.getRole() == MemberRole.OWNER)
                        );

        if (!isOwner) {
            throw new RuntimeException("NOT_AUTHORIZED");
        }

        boolean alreadyMember = team.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(receiver.getId()));

        if (alreadyMember) {
            throw new RuntimeException("ALREADY_MEMBER");
        }

        boolean alreadyInvited = invitationRepository
                .findByTeamIdAndReceiverIdAndStatus(teamId, receiver.getId(), InvitationStatus.PENDING)
                .isPresent();

        if (alreadyInvited) {
            throw new RuntimeException("ALREADY_INVITED");
        }

        Invitation invitation = invitationRepository.save(
                Invitation.builder()
                        .teamId(teamId)
                        .senderId(senderId)
                        .receiverId(receiver.getId())
                        .type(InvitationType.PLAYER_INVITATION)
                        .status(InvitationStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        return InvitationResponse.from(invitation);
    }

    //-------------------------------INVITE TEAM------------------------------------------------------------------------
    @Transactional
    public InvitationResponse inviteTeam(
            String senderTeamId,
            String receiverTeamCode,
            String senderUserId
    ) {

        Team senderTeam = teamRepository.findById(senderTeamId)
                .orElseThrow(() -> new RuntimeException("SENDER_TEAM_NOT_FOUND"));

        if (!senderTeam.getOwnerId().equals(senderUserId)) {
            throw new RuntimeException("NOT_TEAM_OWNER");
        }

        Team receiverTeam = teamRepository.findByTeamCode(receiverTeamCode)
                .orElseThrow(() -> new RuntimeException("RECEIVER_TEAM_NOT_FOUND"));

        if (senderTeam.getId().equals(receiverTeam.getId())) {
            throw new RuntimeException("CANNOT_INVITE_OWN_TEAM");
        }

        boolean alreadyInvited = invitationRepository
                .findBySenderTeamIdAndReceiverTeamIdAndStatus(
                        senderTeamId,
                        receiverTeam.getId(),
                        InvitationStatus.PENDING
                )
                .isPresent();

        if (alreadyInvited) {
            throw new RuntimeException("TEAM_ALREADY_INVITED");
        }

        Invitation invitation = Invitation.builder()
                .senderId(senderUserId)
                .receiverId(receiverTeam.getOwnerId())
                .senderTeamId(senderTeamId)
                .senderTeamName(senderTeam.getName())
                .receiverTeamId(receiverTeam.getId())
                .receiverTeamName(receiverTeam.getName())
                .type(InvitationType.TEAM_MATCH_INVITATION)
                .status(InvitationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        invitationRepository.save(invitation);

        return InvitationResponse.from(invitation);
    }

    //-------------------------------ACCEPT INVITATION------------------------------------------------------------------
    @Transactional
    public void acceptInvitation(String invitationId, String userId) {

        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("INVITATION_NOT_FOUND"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("INVITATION_ALREADY_PROCESSED");
        }

        if (invitation.getType() == InvitationType.PLAYER_INVITATION) {

            if (!invitation.getReceiverId().equals(userId)) {
                throw new RuntimeException("NOT_ALLOWED");
            }

            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitation.setUpdatedAt(LocalDateTime.now());
            invitationRepository.save(invitation);

            acceptPlayerInvitation(invitation, userId);
            return;
        }

        if (invitation.getType() == InvitationType.TEAM_MATCH_INVITATION) {

            Team receiverTeam = teamRepository.findById(invitation.getReceiverTeamId())
                    .orElseThrow(() -> new RuntimeException("TEAM_NOT_FOUND"));

            if (!receiverTeam.getOwnerId().equals(userId)) {
                throw new RuntimeException("NOT_ALLOWED");
            }

            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitation.setUpdatedAt(LocalDateTime.now());
            invitationRepository.save(invitation);

            notifyMatchAccepted(invitation);
        }
    }

    //--------------------------NOTIFICATION ACCEPTATION PLAYER---------------------------------------------------------
    private void acceptPlayerInvitation(Invitation invitation, String userId) {

        Team team = teamRepository.findById(invitation.getTeamId())
                .orElseThrow(() -> new RuntimeException("TEAM_NOT_FOUND"));

        boolean alreadyMember = team.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(userId));

        if (!alreadyMember) {
            team.getMembers().add(
                    Team.TeamMember.builder()
                            .userId(userId)
                            .role(MemberRole.MEMBER)
                            .build()
            );
            teamRepository.save(team);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if (!user.getTeamIds().contains(team.getId())) {
            user.getTeamIds().add(team.getId());
            userRepository.save(user);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("teamId", team.getId());
        data.put("teamName", team.getName());

        notificationService.send(
                userId,
                invitation.getSenderId(),
                "Invitation acceptée",
                user.getFirstname() + " " + user.getLastname() + " a rejoint l'équipe " + team.getName(),
                NotificationType.INVITATION_ACCEPTED,
                team.getId(),
                data
        );
    }

    //-----------------------------------------NOTIFICATION ACCEPTATION TEAM--------------------------------------------
    private void notifyMatchAccepted(Invitation invitation) {

        Team senderTeam = teamRepository.findById(invitation.getSenderTeamId())
                .orElseThrow(() -> new RuntimeException("SENDER_TEAM_NOT_FOUND"));

        Team receiverTeam = teamRepository.findById(invitation.getReceiverTeamId())
                .orElseThrow(() -> new RuntimeException("RECEIVER_TEAM_NOT_FOUND"));

        Map<String, Object> data = new HashMap<>();
        data.put("senderTeamId", invitation.getSenderTeamId());
        data.put("receiverTeamId", invitation.getReceiverTeamId());
        data.put("receiverTeamName", receiverTeam.getName());

        notificationService.send(
                receiverTeam.getOwnerId(),
                invitation.getSenderId(),
                "Invitation acceptée",
                receiverTeam.getName() + " a accepté votre invitation de match",
                NotificationType.INVITATION_ACCEPTED,
                senderTeam.getId(),
                data
        );
    }

    //--------------------------REFUSE INVITATION-----------------------------------------------------------------------
    @Transactional
    public void refuseInvitation(String invitationId, String userId) {

        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("INVITATION_NOT_FOUND"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("INVITATION_ALREADY_PROCESSED");
        }

        if (invitation.getType() == InvitationType.PLAYER_INVITATION) {

            if (!invitation.getReceiverId().equals(userId)) {
                throw new RuntimeException("NOT_ALLOWED");
            }

            invitation.setStatus(InvitationStatus.REJECTED);
            invitation.setUpdatedAt(LocalDateTime.now());
            invitationRepository.save(invitation);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

            Team team = teamRepository.findById(invitation.getTeamId())
                    .orElseThrow(() -> new RuntimeException("TEAM_NOT_FOUND"));

            Map<String, Object> data = new HashMap<>();
            data.put("teamId", team.getId());
            data.put("teamName", team.getName());

            notificationService.send(
                    userId,
                    invitation.getSenderId(),
                    "Invitation refusée",
                    user.getFirstname() + " " + user.getLastname() + " a refusé votre invitation",
                    NotificationType.INVITATION_REJECTED,
                    team.getId(),
                    data
            );

            return;
        }

        if (invitation.getType() == InvitationType.TEAM_MATCH_INVITATION) {

            Team receiverTeam = teamRepository.findById(invitation.getReceiverTeamId())
                    .orElseThrow(() -> new RuntimeException("TEAM_NOT_FOUND"));

            if (!receiverTeam.getOwnerId().equals(userId)) {
                throw new RuntimeException("NOT_ALLOWED");
            }

            invitation.setStatus(InvitationStatus.REJECTED);
            invitation.setUpdatedAt(LocalDateTime.now());
            invitationRepository.save(invitation);

            Team senderTeam = teamRepository.findById(invitation.getSenderTeamId())
                    .orElseThrow(() -> new RuntimeException("SENDER_TEAM_NOT_FOUND"));

            Map<String, Object> data = new HashMap<>();
            data.put("senderTeamId", invitation.getSenderTeamId());
            data.put("receiverTeamId", receiverTeam.getId());
            data.put("receiverTeamName", receiverTeam.getName());

            notificationService.send(
                    receiverTeam.getOwnerId(),
                    invitation.getSenderId(),
                    "Invitation refusée",
                    receiverTeam.getName() + " a refusé votre invitation de match",
                    NotificationType.INVITATION_REJECTED,
                    senderTeam.getId(),
                    data
            );
        }
    }

    //----------------------------------CANCEL INVITATION---------------------------------------------------------------
    @Transactional
    public void cancelInvitation(String invitationId, String userId) {

        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("INVITATION_NOT_FOUND"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("INVITATION_ALREADY_PROCESSED");
        }

        if (!invitation.getSenderId().equals(userId)) {
            throw new RuntimeException("NOT_ALLOWED");
        }

        invitation.setStatus(InvitationStatus.CANCELLED);
        invitation.setUpdatedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    //-----------------GET PENDING INVITATIONS--------------------------------------------------------------------------
    public List<InvitationResponse> getPendingInvitations(String userId) {

        List<Invitation> invitations = new ArrayList<>();

        invitations.addAll(
                invitationRepository.findByTypeAndReceiverIdAndStatus(
                        InvitationType.PLAYER_INVITATION,
                        userId,
                        InvitationStatus.PENDING
                )
        );

        List<Team> ownedTeams = teamRepository.findByOwnerId(userId);

        for (Team team : ownedTeams) {
            invitations.addAll(
                    invitationRepository.findByTypeAndReceiverTeamIdAndStatus(
                            InvitationType.TEAM_MATCH_INVITATION,
                            team.getId(),
                            InvitationStatus.PENDING
                    )
            );
        }

        return invitations.stream()
                .map(InvitationResponse::from)
                .collect(Collectors.toList());
    }
    //----------------------------------GET TEAM MATCH INVITATIONS------------------------------------------------------
    public List<InvitationResponse> getTeamMatchInvitations(String userId) {

        List<Team> ownedTeams = teamRepository.findByOwnerId(userId);

        if (ownedTeams.isEmpty()) {
            return List.of();
        }

        List<Invitation> invitations = invitationRepository.findByTypeAndSenderIdOrReceiverId(
                InvitationType.TEAM_MATCH_INVITATION,
                userId,
                userId
        );

        for (Team team : ownedTeams) {
            invitations.addAll(
                    invitationRepository.findByTypeAndReceiverTeamId(
                            InvitationType.TEAM_MATCH_INVITATION,
                            team.getId()
                    )
            );
        }

        Map<String, Invitation> invitationsUniques = invitations.stream()
                .collect(Collectors.toMap(
                        Invitation::getId,
                        inv -> inv,
                        (existing, replacement) -> existing
                ));

        return invitationsUniques.values().stream()
                .map(InvitationResponse::from)
                .collect(Collectors.toList());
    }
}