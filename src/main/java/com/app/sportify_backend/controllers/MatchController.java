package com.app.sportify_backend.controllers;

import com.app.sportify_backend.dto.CreateMatchRequest;
import com.app.sportify_backend.dto.MatchResponse;
import com.app.sportify_backend.dto.UpdateScoreRequest;
import com.app.sportify_backend.models.MatchStatus;
import com.app.sportify_backend.services.MatchService;
import com.app.sportify_backend.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MatchController {

    private final MatchService matchService;

    // -------------------- CREATE MATCH --------------------
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public MatchResponse createMatch(
            @RequestBody CreateMatchRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return matchService.createMatch(request, user.getId());
    }

    // -------------------- GET MATCH BY ID --------------------
    @GetMapping("/{matchId}")
    @PreAuthorize("isAuthenticated()")
    public MatchResponse getMatch(@PathVariable String matchId) {
        return matchService.getMatchById(matchId);
    }

    // -------------------- GET MATCHES BY TEAM --------------------
    @GetMapping("/team/{teamId}")
    @PreAuthorize("isAuthenticated()")
    public List<MatchResponse> getTeamMatches(@PathVariable String teamId) {
        return matchService.getTeamMatches(teamId);
    }

    // -------------------- UPDATE MATCH STATUS --------------------
    @PatchMapping("/{matchId}/status")
    @PreAuthorize("isAuthenticated()")
    public MatchResponse updateMatchStatus(
            @PathVariable String matchId,
            @RequestParam MatchStatus status,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return matchService.updateMatchStatus(matchId, status, user.getId());
    }

    // -------------------- UPDATE MATCH SCORE --------------------
    @PutMapping("/{matchId}/score")
    @PreAuthorize("isAuthenticated()")
    public MatchResponse updateMatchScore(
            @PathVariable String matchId,
            @RequestBody UpdateScoreRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return matchService.updateMatchScore(matchId, request, user.getId());
    }

    // -------------------- CANCEL MATCH --------------------
    @DeleteMapping("/{matchId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelMatch(
            @PathVariable String matchId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        matchService.cancelMatch(matchId, user.getId());
    }

    // -------------------- CONFIRM MATCH --------------------
    @PostMapping("/{matchId}/confirm")
    @PreAuthorize("isAuthenticated()")
    public MatchResponse confirmMatch(
            @PathVariable String matchId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return matchService.confirmMatch(matchId, user.getId());
    }

    // -------------------- GET MATCHES BY STATUS --------------------
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public List<MatchResponse> getMatchesByStatus(@PathVariable MatchStatus status) {
        return matchService.getMatchesByStatus(status);
    }

    // -------------------- COUNT TODAY'S MATCHES --------------------
    @GetMapping("/today/count")
    @PreAuthorize("isAuthenticated()")
    public long getTodayMatchesCount() {
        return matchService.countTodayMatches();
    }
}