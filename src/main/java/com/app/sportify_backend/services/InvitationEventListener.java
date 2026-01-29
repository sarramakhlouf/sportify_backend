/*package com.app.sportify_backend.services;

import com.app.sportify_backend.models.*;
import com.app.sportify_backend.repositories.TeamRepository;
import com.app.sportify_backend.repositories.UserRepository;
import com.app.sportify_backend.utils.InvitationAcceptedEvent;
import com.app.sportify_backend.utils.InvitationCreatedEvent;
import com.app.sportify_backend.utils.InvitationRejectedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvitationEventListener {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    @EventListener
    public void onCreated(InvitationCreatedEvent event) {
        Invitation inv = event.invitation();

        if (inv.getType() == InvitationType.PLAYER_INVITATION) {
            Team team = teamRepository.findById(inv.getTeamId()).orElseThrow();

            notificationService.send(
                    inv.getReceiverId(),
                    "Invitation d’équipe",
                    "Vous avez reçu une invitation pour rejoindre " + team.getName(),
                    NotificationType.INVITATION_RECEIVED,
                    inv.getId()
            );
        } else if (inv.getType() == InvitationType.TEAM_MATCH_INVITATION) {
            Team senderTeam = teamRepository.findById(inv.getTeamId()).orElseThrow();
            Team receiverTeam = teamRepository.findByOwnerId(inv.getReceiverId())
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Receiver team not found"));

            notificationService.send(
                    inv.getReceiverId(),
                    "Invitation de match",
                    "Le propriétaire de l'équipe " + senderTeam.getName() + " vous a invité à un match",
                    NotificationType.MATCH_INVITATION,
                    inv.getId()
            );
        }
    }

    @EventListener
    public void onAccepted(InvitationAcceptedEvent event) {

        Invitation inv = event.invitation();
        User user = userRepository.findById(inv.getReceiverId()).orElseThrow();

        notificationService.send(
                inv.getSenderId(),
                "Invitation acceptée",
                user.getFirstname() + " a accepté votre invitation",
                NotificationType.INVITATION_ACCEPTED,
                inv.getId()
        );
    }

    @EventListener
    public void onRejected(InvitationRejectedEvent event) {

        Invitation inv = event.invitation();
        User user = userRepository.findById(inv.getReceiverId()).orElseThrow();

        notificationService.send(
                inv.getSenderId(),
                "Invitation refusée",
                user.getFirstname() + " a refusé votre invitation",
                NotificationType.INVITATION_REJECTED,
                inv.getId()
        );
    }
}*/

