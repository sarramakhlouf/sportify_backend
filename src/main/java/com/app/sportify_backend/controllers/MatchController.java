package com.app.sportify_backend.controllers;

import com.app.sportify_backend.dto.CreateMatchRequest;
import com.app.sportify_backend.dto.MatchResponse;
import com.app.sportify_backend.dto.PitchDTO;
import com.app.sportify_backend.models.MatchStatus;
import com.app.sportify_backend.models.Pitch;
import com.app.sportify_backend.services.MatchService;
import com.app.sportify_backend.services.PitchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MatchController {

    private final MatchService matchService;
    private final PitchService pitchService;

    @PostMapping
    public ResponseEntity<?> createMatch(
            @RequestBody CreateMatchRequest request,
            @RequestHeader("X-User-Id") String userId) {
        try {
            MatchResponse match = matchService.createMatch(request, userId);
            return ResponseEntity.ok(match);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<MatchResponse>> getTeamMatches(@PathVariable String teamId) {
        List<MatchResponse> matches = matchService.getTeamMatches(teamId);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<?> getMatch(@PathVariable String matchId) {
        try {
            MatchResponse match = matchService.getMatchById(matchId);
            return ResponseEntity.ok(match);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PatchMapping("/{matchId}/status")
    public ResponseEntity<?> updateMatchStatus(
            @PathVariable String matchId,
            @RequestParam MatchStatus status,
            @RequestHeader("X-User-Id") String userId) {
        try {
            MatchResponse match = matchService.updateMatchStatus(matchId, status, userId);
            return ResponseEntity.ok(match);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/{matchId}")
    public ResponseEntity<?> cancelMatch(
            @PathVariable String matchId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            matchService.cancelMatch(matchId, userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/{matchId}/confirm")
    public ResponseEntity<?> confirmMatch(
            @PathVariable String matchId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            MatchResponse match = matchService.confirmMatch(matchId, userId);
            return ResponseEntity.ok(match);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MatchResponse>> getMatchesByStatus(@PathVariable MatchStatus status) {
        List<MatchResponse> matches = matchService.getMatchesByStatus(status);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/pitches")
    public ResponseEntity<List<PitchDTO>> getAllActivePitches() {
        List<Pitch> pitches = pitchService.getAllActivePitches();
        List<PitchDTO> pitchDTOs = pitches.stream()
                .map(this::convertToPitchDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pitchDTOs);
    }

    @GetMapping("/pitches/city/{city}")
    public ResponseEntity<List<PitchDTO>> getPitchesByCity(@PathVariable String city) {
        List<Pitch> pitches = pitchService.getPitchesByCity(city);
        List<PitchDTO> pitchDTOs = pitches.stream()
                .map(this::convertToPitchDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pitchDTOs);
    }

    private PitchDTO convertToPitchDTO(Pitch pitch) {
        PitchDTO dto = new PitchDTO();
        dto.setId(pitch.getId());
        dto.setName(pitch.getName());
        dto.setAddress(pitch.getAddress());
        dto.setCity(pitch.getCity());
        dto.setPrice(pitch.getPrice());
        dto.setSize(pitch.getSize());
        dto.setRating(pitch.getRating());
        dto.setSurfaceType(pitch.getSurfaceType());
        dto.setImageUrl(pitch.getImageUrl());
        dto.setActive(pitch.isActive());
        return dto;
    }
}