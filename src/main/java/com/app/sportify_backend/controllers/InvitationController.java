package com.app.sportify_backend.controllers;

import com.app.sportify_backend.dto.InviteRequest;
import com.app.sportify_backend.models.Invitation;
import com.app.sportify_backend.services.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public Invitation invitePlayer(@RequestBody InviteRequest request) {
        return invitationService.invitePlayer(
                request.getTeamId(),
                request.getSenderId(),
                request.getPlayerCode()
        );
    }

    @PostMapping("/{id}/accept")
    @ResponseStatus(HttpStatus.OK)
    public void acceptInvitation(@PathVariable String id, @RequestParam String userId) {
        invitationService.acceptInvitation(id, userId);
    }

    @PostMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.OK)
    public void refuseInvitation(@PathVariable String id, @RequestParam String userId) {
        invitationService.refuseInvitation(id, userId);
    }

    @GetMapping("/pending/{userId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public List<Invitation> getPendingInvitations(@PathVariable String userId) {
        return invitationService.getPendingInvitations(userId);
    }
}
