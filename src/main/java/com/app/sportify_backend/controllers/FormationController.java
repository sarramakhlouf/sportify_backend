package com.app.sportify_backend.controllers;

import com.app.sportify_backend.dto.ChangeFormationTypeRequest;
import com.app.sportify_backend.dto.CreateFormationRequest;
import com.app.sportify_backend.dto.FormationExistsResponse;
import com.app.sportify_backend.dto.UpdatePlayerPositionRequest;
import com.app.sportify_backend.models.Formation;
import com.app.sportify_backend.services.FormationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/formations")
@RequiredArgsConstructor
public class FormationController {

    private final FormationService formationService;

    @PostMapping
    public ResponseEntity<Formation> createFormation(
            @RequestBody CreateFormationRequest request) {
        Formation formation = formationService.createFormation(
                request.getTeamId(),
                request.getFormationType(),
                request.getOwnerId(),
                request.getOwnerPreferredPosition()
        );
        return ResponseEntity.ok(formation);
    }

    @PutMapping("/{formationId}/positions")
    public ResponseEntity<Formation> updatePositions(
            @PathVariable String formationId,
            @RequestBody UpdatePlayerPositionRequest request) {
        Formation formation = formationService.updatePlayerPositions(
                formationId,
                request.getPositions()
        );
        return ResponseEntity.ok(formation);
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<Formation> getFormation(
            @PathVariable String teamId) {

        return formationService.getFormation(teamId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/team/{teamId}/exists")
    public ResponseEntity<FormationExistsResponse> checkFormationExists(
            @PathVariable String teamId) {
        boolean exists = formationService.formationExists(teamId);
        return ResponseEntity.ok(new FormationExistsResponse(exists));
    }

    @PutMapping("/{formationId}/formation-type")
    public ResponseEntity<Formation> updateFormationType(
            @PathVariable String formationId,
            @RequestBody ChangeFormationTypeRequest request) {
        Formation updated = formationService.updateFormationType(
                formationId,
                request.getFormationType()
        );
        return ResponseEntity.ok(updated);
    }

}