package com.app.sportify_backend.services;

import com.app.sportify_backend.dto.TeamPlayerResponse;
import com.app.sportify_backend.models.*;
import com.app.sportify_backend.repositories.TeamRepository;
import com.app.sportify_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public Team createTeam(Team team, MultipartFile image) throws IOException {
        team.setIsActivated(false);
        team = teamRepository.save(team);

        if (image != null && !image.isEmpty()) {
            String fileExtension = image.getOriginalFilename()
                    .substring(image.getOriginalFilename().lastIndexOf("."));
            String filename = team.getId() + "_" + UUID.randomUUID() + fileExtension;

            Path uploadPath = Paths.get("uploads/teams/" + filename);
            Files.createDirectories(uploadPath.getParent());
            Files.write(uploadPath, image.getBytes());

            team.setLogoUrl("/uploads/teams/" + filename);
            team = teamRepository.save(team);
        }
        team.setTeamCode(generateUniqueTeamCode());

        return teamRepository.save(team);
    }

    public List<Team> getTeamsByOwner(String ownerId) {
        return teamRepository.findByOwnerId(ownerId);
    }

    public Optional<Team> getTeamById(String id) {
        return teamRepository.findById(id);
    }

    public Team updateTeam(String id, Team updatedTeam) {
        return teamRepository.findById(id).map(team -> {
            team.setName(updatedTeam.getName());
            team.setCity(updatedTeam.getCity());
            team.setColor(updatedTeam.getColor());
            team.setLogoUrl(updatedTeam.getLogoUrl());
            team.setIsActivated(updatedTeam.getIsActivated());
            return teamRepository.save(team);
        }).orElseThrow(() -> new RuntimeException("Team not found"));
    }

    public Team activateTeam(String teamId, String ownerId) {
        List<Team> ownerTeams = teamRepository.findByOwnerId(ownerId);

        for (Team t : ownerTeams) {
            if (t.getId().equals(teamId)) {
                t.setIsActivated(true);
            } else {
                t.setIsActivated(false);
            }
            teamRepository.save(t);
        }

        return teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
    }

    public Team deactivateTeam(String teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        team.setIsActivated(false);
        return teamRepository.save(team);
    }

    public List<TeamPlayerResponse> getTeamPlayers(String teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("TEAM_NOT_FOUND"));

        return team.getMembers().stream().map(member -> {

            User user = userRepository.findById(member.getUserId())
                    .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

            return TeamPlayerResponse.builder()
                    .userId(user.getId())
                    .firstname(user.getFirstname())
                    .lastname(user.getLastname())
                    .email(user.getEmail())
                    .profileImage(user.getProfileImageUrl())
                    .playerCode(user.getPlayerCode())
                    .role(member.getRole().name()) // OWNER / MEMBER
                    .build();
        }).toList();
    }

    private String generateUniqueTeamCode() {
        String code;
        do {
            code = CodeGenerator.generateTeamCode();
        } while (teamRepository.findByTeamCode(code).isPresent());
        return code;
    }

    public void deleteTeam(String id) {
        teamRepository.deleteById(id);
    }
}
