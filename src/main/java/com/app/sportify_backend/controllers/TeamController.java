package com.app.sportify_backend.controllers;

import com.app.sportify_backend.dto.TeamPlayerResponse;
import com.app.sportify_backend.dto.UpdateTeamRequest;
import com.app.sportify_backend.dto.UserTeamsResponse;
import com.app.sportify_backend.models.Team;
import com.app.sportify_backend.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping(consumes = "multipart/form-data")
    public Team createTeam(
            @RequestPart(value = "data", required = true) String teamJson,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Team team = mapper.readValue(teamJson, Team.class);

        return teamService.createTeam(team, image);
    }

    @GetMapping("/owner/{ownerId}")
    public List<Team> getTeamsByOwner(@PathVariable String ownerId) {
        return teamService.getTeamsByOwner(ownerId);
    }

    @GetMapping("/member/{userId}")
    public List<Team> getTeamsWhereUserIsMember(@PathVariable String userId) {
        return teamService.getTeamsWhereUserIsMember(userId);
    }

    @GetMapping("/user/{userId}")
    public UserTeamsResponse getUserTeams(@PathVariable String userId) {
        return teamService.getUserTeams(userId);
    }

    /*@PutMapping("/{id}")
    public Team updateTeam(@PathVariable String id, @RequestBody Team team) {
        return teamService.updateTeam(id, team);
    }*/

    @PostMapping(value = "/{id}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Team updateTeam(
            @PathVariable String id,
            @RequestPart("data") UpdateTeamRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        System.out.println("CONTROLLER NAME = " + request.getName());
        return teamService.updateTeam(id, request, image);
    }

    @PutMapping("/activate/{teamId}/owner/{ownerId}")
    public Team activateTeam(@PathVariable String teamId, @PathVariable String ownerId) {
        return teamService.activateTeam(teamId, ownerId);
    }

    @PutMapping("/deactivate/{teamId}")
    public Team deactivateTeam(@PathVariable String teamId) {
        return teamService.deactivateTeam(teamId);
    }

    @GetMapping("/{teamId}/players")
    public List<TeamPlayerResponse> getTeamPlayers(@PathVariable String teamId) {
        return teamService.getTeamPlayers(teamId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeam(@PathVariable String id) {
        teamService.deleteTeam(id);
        return ResponseEntity.ok().body("Team deleted successfully");
    }

    @GetMapping("/{id}")
    public Team getTeamById(@PathVariable String id) {
        return teamService.getTeamById(id);
    }
}
