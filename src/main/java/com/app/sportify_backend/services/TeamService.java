package com.app.sportify_backend.services;

import com.app.sportify_backend.dto.TeamPlayerResponse;
import com.app.sportify_backend.dto.UpdateTeamRequest;
import com.app.sportify_backend.dto.PlayerTeamsResponse;
import com.app.sportify_backend.models.*;
import com.app.sportify_backend.utils.CodeGenerator;
import com.app.sportify_backend.repositories.TeamRepository;
import com.app.sportify_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public Team createTeam(Team team, MultipartFile image) throws IOException {
        team.setIsActivated(false);

        team.setTeamCode(generateUniqueTeamCode());

        User owner = userRepository.findById(team.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        List<Team.TeamMember> members = new ArrayList<>();
        members.add(Team.TeamMember.builder()
                .userId(owner.getId())
                .userFirstName(owner.getFirstname())
                .userLastName(owner.getLastname())
                .role(MemberRole.OWNER)
                .build());

        team.setMembers(members);

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

        return team;
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String activeTeamId = user.getActiveTeamId();

        List<Team> ownedTeams = getTeamsByOwner(userId);
        List<Team> memberTeams = getTeamsWhereUserIsMember(userId);

        ownedTeams.forEach(team -> {
            team.setIsActivated(team.getId().equals(activeTeamId));
        });

        memberTeams.forEach(team -> {
            team.setIsActivated(team.getId().equals(activeTeamId));
        });

        return PlayerTeamsResponse.builder()
                .ownedTeams(ownedTeams)
                .memberTeams(memberTeams)
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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActiveTeamId(teamId);
        userRepository.save(user);

        return teamToActivate;
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
            userRepository.findById(userId).ifPresent(user -> {
                user.getTeamIds().remove(id);
                userRepository.save(user);
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

    public void leaveTeam(String teamId, String userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("TEAM_NOT_FOUND"));

        if (team.getOwnerId().equals(userId)) {
            throw new RuntimeException("Le propriétaire ne peut pas quitter son équipe");
        }

        boolean isMember = team.getMembers().stream()
                .anyMatch(member -> member.getUserId().equals(userId));

        if (!isMember) {
            throw new RuntimeException("L'utilisateur n'est pas membre de cette équipe");
        }

        User leavingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        team.getMembers().removeIf(member -> member.getUserId().equals(userId));
        teamRepository.save(team);

        if (teamId.equals(leavingUser.getActiveTeamId())) {
            leavingUser.setActiveTeamId(null);
            userRepository.save(leavingUser);
        }

        // Données supplémentaires pour le front
        Map<String, Object> data = new HashMap<>();
        data.put("teamId", teamId);
        data.put("teamName", team.getName());
        data.put("playerName", leavingUser.getFirstname() + " " + leavingUser.getLastname());

        notificationService.send(
                team.getOwnerId(),                                          // recipientId  → owner
                userId,                                                     // senderId     → membre qui quitte
                "Un joueur a quitté votre équipe",                         // title
                leavingUser.getFirstname() + " " + leavingUser.getLastname()
                        + " a quitté l'équipe " + team.getName(),          // message
                NotificationType.TEAM_LEFT,                                 // type
                teamId,                                                     // referenceId
                data                                                        // data
        );
    }

}
