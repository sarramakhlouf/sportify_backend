package com.app.sportify_backend.controllers;

import com.app.sportify_backend.models.Team;
import com.app.sportify_backend.services.TeamService;
import lombok.RequiredArgsConstructor;
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

    @PutMapping("/{id}")
    public Team updateTeam(@PathVariable String id, @RequestBody Team team) {
        return teamService.updateTeam(id, team);
    }

    @PutMapping("/activate/{teamId}/owner/{ownerId}")
    public Team activateTeam(@PathVariable String teamId, @PathVariable String ownerId) {
        return teamService.activateTeam(teamId, ownerId);
    }

    @PutMapping("/deactivate/{teamId}")
    public Team deactivateTeam(@PathVariable String teamId) {
        return teamService.deactivateTeam(teamId);
    }

    @DeleteMapping("/{id}")
    public void deleteTeam(@PathVariable String id) {
        teamService.deleteTeam(id);
    }
}
