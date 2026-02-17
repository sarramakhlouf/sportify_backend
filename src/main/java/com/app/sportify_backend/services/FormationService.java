package com.app.sportify_backend.services;

import com.app.sportify_backend.dto.UpdatePlayerPositionRequest;
import com.app.sportify_backend.models.*;
import com.app.sportify_backend.repositories.FormationRepository;
import com.app.sportify_backend.repositories.TeamRepository;
import com.app.sportify_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormationService {

    private final FormationRepository formationRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public Formation createFormation(String teamId,
                                     FormationType formationType,
                                     String ownerId,
                                     Position ownerPreferredPosition) {

        Optional<Formation> existingFormation =
                formationRepository.findByTeamId(teamId);

        if (existingFormation.isPresent()) {
            return existingFormation.get();
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        List<Formation.PlayerPosition> defaultPositions =
                getDefaultPositions(formationType, team.getMembers(), ownerId, ownerPreferredPosition);

        Formation formation = Formation.builder()
                .teamId(teamId)
                .formationType(formationType)
                .playerPositions(defaultPositions)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return formationRepository.save(formation);
    }

    public boolean formationExists(String teamId) {
        return formationRepository.findByTeamId(teamId).isPresent();
    }

    public Optional<Formation> getFormation(String teamId) {
        return formationRepository.findByTeamId(
                teamId
        );
    }

    private List<Formation.PlayerPosition> getDefaultPositions(
            FormationType type,
            List<Team.TeamMember> members,
            String ownerId,
            Position ownerPreferredPosition) {

        Map<Position, CoordinatePair> defaultCoords =
                getDefaultCoordinates(type);

        List<Formation.PlayerPosition> positions = new ArrayList<>();
        List<Position> positionOrder = getPositionOrder(type);

        Team.TeamMember owner = members.stream()
                .filter(m -> m.getUserId().equals(ownerId))
                .findFirst()
                .orElse(null);

        if (owner != null && ownerPreferredPosition != null &&
                positionOrder.contains(ownerPreferredPosition)) {

            User user = userRepository.findById(owner.getUserId()).orElse(null);
            CoordinatePair coords = defaultCoords.get(ownerPreferredPosition);

            String playerName;
            String playerImageUrl = null;

            if (user != null) {
                playerName = user.getFirstname() + " " + user.getLastname();
                playerImageUrl = user.getProfileImageUrl();
            } else {
                playerName = owner.getUserFirstName() + " " + owner.getUserLastName();
            }

            positions.add(Formation.PlayerPosition.builder()
                    .userId(owner.getUserId())
                    .playerName(playerName)
                    .playerImageUrl(playerImageUrl)
                    .position(ownerPreferredPosition)
                    .xPosition(coords.x)
                    .yPosition(coords.y)
                    .build());
        }
        List<Team.TeamMember> otherMembers = members.stream()
                .filter(m -> !m.getUserId().equals(ownerId))
                .toList();

        int memberIndex = 0;
        for (Position position : positionOrder) {
            if (ownerPreferredPosition != null && position == ownerPreferredPosition) {
                continue;
            }

            CoordinatePair coords = defaultCoords.get(position);

            if (memberIndex < otherMembers.size()) {
                Team.TeamMember member = otherMembers.get(memberIndex);
                User user = userRepository.findById(member.getUserId()).orElse(null);

                String playerName;
                String playerImageUrl = null;

                if (user != null) {
                    playerName = user.getFirstname() + " " + user.getLastname();
                    playerImageUrl = user.getProfileImageUrl();
                } else {
                    playerName = member.getUserFirstName() + " " + member.getUserLastName();
                }

                positions.add(Formation.PlayerPosition.builder()
                        .userId(member.getUserId())
                        .playerName(playerName)
                        .playerImageUrl(playerImageUrl)
                        .position(position)
                        .xPosition(coords.x)
                        .yPosition(coords.y)
                        .build());

                memberIndex++;
            } else {
                positions.add(Formation.PlayerPosition.builder()
                        .userId(null)
                        .playerName("Position libre")
                        .playerImageUrl(null)
                        .position(position)
                        .xPosition(coords.x)
                        .yPosition(coords.y)
                        .build());
            }
        }

        return positions;
    }

    private Map<Position, CoordinatePair> getDefaultCoordinates(FormationType type) {
        Map<Position, CoordinatePair> coords = new HashMap<>();

        switch (type) {
            case FORMATION_2_1_2_1:
                coords.put(Position.GK, new CoordinatePair(0.5, 0.95));
                coords.put(Position.LB, new CoordinatePair(0.25, 0.75));
                coords.put(Position.RB, new CoordinatePair(0.75, 0.75));
                coords.put(Position.CDM, new CoordinatePair(0.5, 0.55));
                coords.put(Position.LW, new CoordinatePair(0.25, 0.35));
                coords.put(Position.RW, new CoordinatePair(0.75, 0.35));
                coords.put(Position.CF, new CoordinatePair(0.5, 0.15));
                break;

            case FORMATION_3_2_1:
                coords.put(Position.GK, new CoordinatePair(0.5, 0.95));
                coords.put(Position.LB, new CoordinatePair(0.2, 0.70));
                coords.put(Position.CDM, new CoordinatePair(0.5, 0.70));
                coords.put(Position.RB, new CoordinatePair(0.8, 0.70));
                coords.put(Position.LW, new CoordinatePair(0.35, 0.40));
                coords.put(Position.RW, new CoordinatePair(0.65, 0.40));
                coords.put(Position.CF, new CoordinatePair(0.5, 0.15));
                break;

            case FORMATION_2_2_2:
                coords.put(Position.GK, new CoordinatePair(0.5, 0.95));
                coords.put(Position.LB, new CoordinatePair(0.25, 0.70));
                coords.put(Position.RB, new CoordinatePair(0.75, 0.70));
                coords.put(Position.CDM, new CoordinatePair(0.35, 0.45));
                coords.put(Position.LW, new CoordinatePair(0.65, 0.45));
                coords.put(Position.RW, new CoordinatePair(0.35, 0.20));
                coords.put(Position.CF, new CoordinatePair(0.65, 0.20));
                break;
        }

        return coords;
    }

    private List<Position> getPositionOrder(FormationType type) {
        switch (type) {
            case FORMATION_2_1_2_1:
                return Arrays.asList(Position.GK, Position.LB, Position.RB,
                        Position.CDM, Position.LW, Position.RW, Position.CF);
            case FORMATION_3_2_1:
                return Arrays.asList(Position.GK, Position.LB, Position.CDM,
                        Position.RB, Position.LW, Position.RW, Position.CF);
            case FORMATION_2_2_2:
                return Arrays.asList(Position.GK, Position.LB, Position.RB,
                        Position.CDM, Position.LW, Position.RW, Position.CF);
            default:
                return Collections.emptyList();
        }
    }

    public Formation updatePlayerPositions(String formationId,
                                           List<UpdatePlayerPositionRequest.PlayerPositionUpdate> updates) {
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation not found"));

        for (UpdatePlayerPositionRequest.PlayerPositionUpdate update : updates) {
            formation.getPlayerPositions().stream()
                    .filter(p -> p.getUserId() != null && p.getUserId().equals(update.getUserId()))
                    .findFirst()
                    .ifPresent(p -> {
                        p.setPosition(update.getPosition());
                        p.setXPosition(update.getXPosition());
                        p.setYPosition(update.getYPosition());
                    });
        }

        formation.setUpdatedAt(LocalDateTime.now());
        return formationRepository.save(formation);
    }

    public Formation updateFormationType(String formationId, FormationType newFormationType) {
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation not found"));

        if (formation.getFormationType() != newFormationType) {
            Team team = teamRepository.findById(formation.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Team not found"));

            List<Formation.PlayerPosition> currentPositions = formation.getPlayerPositions();

            List<Formation.PlayerPosition> updatedPositions =
                    reorganizePositionsForNewFormation(newFormationType, currentPositions);

            formation.setFormationType(newFormationType);
            formation.setPlayerPositions(updatedPositions);
            formation.setUpdatedAt(LocalDateTime.now());

            return formationRepository.save(formation);
        }

        return formation;
    }

    private List<Formation.PlayerPosition> reorganizePositionsForNewFormation(
            FormationType newFormationType,
            List<Formation.PlayerPosition> currentPositions) {

        Map<Position, CoordinatePair> newCoords = getDefaultCoordinates(newFormationType);
        List<Position> newPositionOrder = getPositionOrder(newFormationType);

        Map<Position, Formation.PlayerPosition> playersByPosition = new HashMap<>();
        List<Formation.PlayerPosition> playersWithoutPosition = new ArrayList<>();

        for (Formation.PlayerPosition currentPos : currentPositions) {
            if (currentPos.getUserId() != null) {
                if (newPositionOrder.contains(currentPos.getPosition())) {
                    playersByPosition.put(currentPos.getPosition(), currentPos);
                } else {
                    playersWithoutPosition.add(currentPos);
                }
            }
        }

        List<Formation.PlayerPosition> result = new ArrayList<>();
        int unassignedIndex = 0;

        for (Position position : newPositionOrder) {
            CoordinatePair coords = newCoords.get(position);

            if (playersByPosition.containsKey(position)) {
                Formation.PlayerPosition existingPlayer = playersByPosition.get(position);

                result.add(Formation.PlayerPosition.builder()
                        .userId(existingPlayer.getUserId())
                        .playerName(existingPlayer.getPlayerName())
                        .playerImageUrl(existingPlayer.getPlayerImageUrl())
                        .position(position)
                        .xPosition(coords.x)
                        .yPosition(coords.y)
                        .build());
            }
            else if (unassignedIndex < playersWithoutPosition.size()) {
                Formation.PlayerPosition unassignedPlayer = playersWithoutPosition.get(unassignedIndex);

                result.add(Formation.PlayerPosition.builder()
                        .userId(unassignedPlayer.getUserId())
                        .playerName(unassignedPlayer.getPlayerName())
                        .playerImageUrl(unassignedPlayer.getPlayerImageUrl())
                        .position(position)
                        .xPosition(coords.x)
                        .yPosition(coords.y)
                        .build());

                unassignedIndex++;
            }
            else {
                result.add(Formation.PlayerPosition.builder()
                        .userId(null)
                        .playerName("Position libre")
                        .playerImageUrl(null)
                        .position(position)
                        .xPosition(coords.x)
                        .yPosition(coords.y)
                        .build());
            }
        }

        return result;
    }


    @lombok.Data
    @lombok.AllArgsConstructor
    private static class CoordinatePair {
        double x;
        double y;
    }
}