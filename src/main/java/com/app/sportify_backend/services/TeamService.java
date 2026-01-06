package com.app.sportify_backend.services;

import com.app.sportify_backend.models.Team;
import com.app.sportify_backend.repositories.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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

    public Team createTeam(Team team, MultipartFile image) throws IOException {
        team.setIsActivated(false);
        team = teamRepository.save(team);

        if (image != null && !image.isEmpty()) {
            String fileExtension = image.getOriginalFilename()
                    .substring(image.getOriginalFilename().lastIndexOf("."));
            String filename = team.getId() + "_" + UUID.randomUUID() + fileExtension;

            // chemin relatif au projet / création des dossiers si nécessaire
            Path uploadPath = Paths.get("uploads/teams/" + filename);
            Files.createDirectories(uploadPath.getParent()); // crée le dossier si pas existant
            Files.write(uploadPath, image.getBytes());

            // stocker le chemin ou URL dans l'entité Team
            team.setLogoUrl("/uploads/teams/" + filename);
            team = teamRepository.save(team);
        }

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

    public void deleteTeam(String id) {
        teamRepository.deleteById(id);
    }
}
