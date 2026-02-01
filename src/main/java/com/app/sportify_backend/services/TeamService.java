package com.app.sportify_backend.services;

import com.app.sportify_backend.dto.TeamPlayerResponse;
import com.app.sportify_backend.dto.UpdateTeamRequest;
import com.app.sportify_backend.dto.PlayerTeamsResponse;
import com.app.sportify_backend.models.*;
import com.app.sportify_backend.utils.CodeGenerator;
import com.app.sportify_backend.repositories.TeamRepository;
import com.app.sportify_backend.repositories.PlayerAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final PlayerAuthRepository playerAuthRepository;

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

    public List<Team> getTeamsWhereUserIsMember(String userId) {
        return teamRepository.findByMembersUserId(userId)
                .stream()
                .filter(team -> !team.getOwnerId().equals(userId))
                .toList();
    }

    public PlayerTeamsResponse getUserTeams(String userId) {
        return PlayerTeamsResponse.builder()
                .ownedTeams(getTeamsByOwner(userId))
                .memberTeams(getTeamsWhereUserIsMember(userId))
                .build();
    }

    /*public Optional<Team> getTeamById(String id) {
        return teamRepository.findById(id);
    }

    public Team updateTeam(String id, Team updatedTeam) {
        return teamRepository.findById(id).map(team -> {
            team.setName(updatedTeam.getName());
            team.setCity(updatedTeam.getCity());
            team.setLogoUrl(updatedTeam.getLogoUrl());
            //team.setIsActivated(updatedTeam.getIsActivated());
            return teamRepository.save(team);
        }).orElseThrow(() -> new RuntimeException("Team not found"));
    }*/

    public Team updateTeam(String teamId, UpdateTeamRequest request, MultipartFile image) {

        System.out.println("NAME RECEIVED = " + request.getName());
        System.out.println("CITY RECEIVED = " + request.getCity());

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("TEAM_NOT_FOUND"));

        team.setName(request.getName());
        team.setCity(request.getCity());

        if (image != null && !image.isEmpty()) {
            try {
                String fileName = team.getId() + "_" + image.getOriginalFilename();
                Path path = Paths.get("uploads/teams/" + fileName);

                Files.createDirectories(path.getParent());
                Files.write(path, image.getBytes());

                team.setLogoUrl("/uploads/teams/" + fileName);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de l'upload de l'image");
            }
        }

        return teamRepository.save(team);
    }

    public Team activateTeam(String teamId, String userId) {
        Team teamToActivate = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        boolean isMemberOrOwner = teamToActivate.getOwnerId().equals(userId)
                || teamToActivate.getMembers().stream()
                .anyMatch(member -> member.getUserId().equals(userId));

        if (!isMemberOrOwner) {
            throw new RuntimeException("L'utilisateur n'est pas autorisé à activer cette équipe");
        }

        List<Team> allUserTeams = teamRepository.findByOwnerId(userId);
        allUserTeams.addAll(teamRepository.findByMembersUserId(userId)
                .stream()
                .filter(t -> !allUserTeams.contains(t))
                .toList());

        for (Team t : allUserTeams) {
            t.setIsActivated(false);
            teamRepository.save(t);
        }

        teamToActivate.setIsActivated(true);
        return teamRepository.save(teamToActivate);
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

            User user = playerAuthRepository.findById(member.getUserId())
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
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        List<String> userIds = new ArrayList<>();

        userIds.add(team.getOwnerId());

        team.getMembers().forEach(member -> {
            if (!userIds.contains(member.getUserId())) {
                userIds.add(member.getUserId());
            }
        });

        userIds.forEach(userId -> {
            playerAuthRepository.findById(userId).ifPresent(user -> {
                user.getTeamIds().remove(id);
                playerAuthRepository.save(user);
            });
        });
        teamRepository.deleteById(id);
    }

    public Team getTeamById(String id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
    }

    public Team getTeamByCode(String teamCode) {
        return teamRepository.findByTeamCode(teamCode)
                .orElseThrow(() -> new RuntimeException("Team not found with code: " + teamCode));
    }

}
