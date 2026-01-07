package com.app.sportify_backend.controllers;

import com.app.sportify_backend.models.Invitation;
import com.app.sportify_backend.services.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping("/invite")
    public Invitation invitePlayer(
            @RequestParam String teamId,
            @RequestParam String playerCode,
            @RequestParam String senderId
    ) {
        return invitationService.invitePlayer(teamId, senderId, playerCode);
    }

    @PostMapping("/{id}/accept")
    public void acceptInvitation(@PathVariable String id, @RequestParam String userId) {
        invitationService.acceptInvitation(id, userId);
    }

    @PostMapping("/{id}/reject")
    public void refuseInvitation(
            @PathVariable String id,
            @RequestParam String userId
    ) {
        invitationService.refuseInvitation(id, userId);
    }

}

