package com.app.sportify_backend.controllers;

import com.app.sportify_backend.dto.CancelInvitationRequest;
import com.app.sportify_backend.dto.InvitationResponse;
import com.app.sportify_backend.dto.InvitePlayerRequest;
import com.app.sportify_backend.dto.InviteTeamRequest;
import com.app.sportify_backend.models.User;
import com.app.sportify_backend.services.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    //---------------INVITE PLAYER---------------------------------------------------------------
    @PostMapping("/invite-player")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public InvitationResponse invitePlayer(
            @RequestBody InvitePlayerRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return invitationService.invitePlayer(
                request.getTeamId(),
                user.getId(),
                request.getPlayerCode()
        );
    }

    //--------------INVITE TEAM---------------------------------------------------------------------
    @PostMapping("/invite-team")
    @PreAuthorize("isAuthenticated()")
    public InvitationResponse inviteTeam(
            @RequestBody InviteTeamRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        return invitationService.inviteTeam(
                request.getSenderTeamId(),
                request.getReceiverTeamCode(),
                user.getId()
        );
    }

    //-----------ACCEPT INVITATION----------------------------------------------------------------
    @PostMapping("/{id}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> acceptInvitation(
            @PathVariable String id,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        invitationService.acceptInvitation(id, user.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Invitation accepted successfully",
                "invitationId", id
        ));
    }

    //----------------REFUSE INVITATION------------------------------------------------------
    @PostMapping("/{id}/refuse")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> refuseInvitation(
            @PathVariable String id,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        invitationService.refuseInvitation(id, user.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Invitation refused",
                "invitationId", id
        ));
    }

    //---------------CANCEL INVITATION-------------------------------------------------------
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cancelInvitation(
            @PathVariable String id,
            @RequestBody CancelInvitationRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        invitationService.cancelInvitation(
                id,
                user.getId(),
                request.getReason(),
                request.getMessage()
        );

        return ResponseEntity.ok(Map.of(
                "message", "Invitation cancelled successfully"
        ));
    }

    //----------------GET PENDING PLAYER INVITATIONS-----------------------------------------------------------
    @GetMapping("/pending/player")
    @PreAuthorize("isAuthenticated()")
    public List<InvitationResponse> getPendingPlayerInvitations(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return invitationService.getPendingPlayerInvitations(user.getId());
    }

    //-----------------------------GET MATCH INVITATIONS----------------------------------------------
    @GetMapping("/team-match")
    public List<InvitationResponse> getTeamMatchInvitations(
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return invitationService.getTeamMatchInvitations(user.getId());
    }


}