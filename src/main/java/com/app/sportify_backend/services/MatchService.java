package com.app.sportify_backend.services;

import com.app.sportify_backend.dto.CreateMatchRequest;
import com.app.sportify_backend.dto.MatchResponse;
import com.app.sportify_backend.dto.PitchDTO;
import com.app.sportify_backend.dto.UpdateScoreRequest;
import com.app.sportify_backend.models.Match;
import com.app.sportify_backend.models.MatchStatus;
import com.app.sportify_backend.models.Pitch;
import com.app.sportify_backend.repositories.MatchRepository;
import com.app.sportify_backend.repositories.PitchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final PitchRepository pitchRepository;

    public MatchResponse createMatch(CreateMatchRequest request, String userId) {
        Pitch pitch = pitchRepository.findById(request.getPitchId())
                .orElseThrow(() -> new RuntimeException("Terrain non trouvé"));

        if (!pitch.isActive()) {
            throw new RuntimeException("Ce terrain n'est pas actif");
        }

        List<Match> existingMatches = matchRepository
                .findByPitchIdAndMatchDate(request.getPitchId(), request.getMatchDate());

        for (Match existingMatch : existingMatches) {
            if (existingMatch.getStatus() != MatchStatus.CANCELLED) {
                LocalTime requestedTime = request.getMatchTime();
                LocalTime existingTime = existingMatch.getMatchTime();

                long minutesDiff = Math.abs(
                        requestedTime.toSecondOfDay() - existingTime.toSecondOfDay()
                ) / 60;

                if (minutesDiff < 120) {
                    throw new RuntimeException(
                            "Le terrain n'est pas disponible à cette heure. " +
                                    "Un match est déjà prévu à " + existingTime
                    );
                }
            }
        }

        Match match = new Match();
        match.setSenderTeamId(request.getSenderTeamId());
        match.setSenderTeamName(request.getSenderTeamName());
        match.setSenderTeamLogoUrl(request.getSenderTeamLogoUrl());
        match.setReceiverTeamId(request.getReceiverTeamId());
        match.setReceiverTeamName(request.getReceiverTeamName());
        match.setReceiverTeamLogoUrl(request.getReceiverTeamLogoUrl());
        match.setPitchId(request.getPitchId());
        match.setMatchDate(request.getMatchDate());
        match.setMatchTime(request.getMatchTime());
        match.setStatus(MatchStatus.PLANNED);
        match.setScore(null); // Score null lors de la création
        match.setCreatedBy(userId);
        match.setCreatedAt(LocalDateTime.now());
        match.setUpdatedAt(LocalDateTime.now());

        Match savedMatch = matchRepository.save(match);
        log.info("Match créé: {} vs {} le {} à {}",
                savedMatch.getSenderTeamName(),
                savedMatch.getReceiverTeamName(),
                savedMatch.getMatchDate(),
                savedMatch.getMatchTime());

        return convertToResponse(savedMatch, pitch);
    }

    public MatchResponse updateMatchScore(String matchId, UpdateScoreRequest request, String userId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match non trouvé"));

        // Validation: le match doit être confirmé ou terminé pour ajouter un score
        if (match.getStatus() == MatchStatus.CANCELLED) {
            throw new RuntimeException("Impossible d'ajouter un score à un match annulé");
        }

        // Validation des scores (pas de scores négatifs)
        if (request.getHomeScore() < 0 || request.getAwayScore() < 0) {
            throw new RuntimeException("Les scores ne peuvent pas être négatifs");
        }

        // Créer le sous-document Score
        Match.Score score = new Match.Score();
        score.setHome(request.getHomeScore());
        score.setAway(request.getAwayScore());

        match.setScore(score);

        // Optionnel: changer le statut du match en FINISHED
        if (match.getStatus() != MatchStatus.COMPLETED) {
            match.setStatus(MatchStatus.COMPLETED);
        }

        match.setUpdatedAt(LocalDateTime.now());
        Match updatedMatch = matchRepository.save(match);

        log.info("Score du match {} mis à jour: {} - {}",
                matchId, request.getHomeScore(), request.getAwayScore());

        Pitch pitch = pitchRepository.findById(match.getPitchId()).orElse(null);
        return convertToResponse(updatedMatch, pitch);
    }

    public List<MatchResponse> getTeamMatches(String teamId) {
        List<Match> matches = matchRepository.findBySenderTeamIdOrReceiverTeamId(teamId, teamId);

        return matches.stream()
                .map(match -> {
                    Pitch pitch = pitchRepository.findById(match.getPitchId())
                            .orElse(null);
                    return convertToResponse(match, pitch);
                })
                .collect(Collectors.toList());
    }

    public MatchResponse getMatchById(String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match non trouvé"));

        Pitch pitch = pitchRepository.findById(match.getPitchId())
                .orElse(null);

        return convertToResponse(match, pitch);
    }

    public MatchResponse updateMatchStatus(String matchId, MatchStatus newStatus, String userId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match non trouvé"));

        match.setStatus(newStatus);
        match.setUpdatedAt(LocalDateTime.now());
        Match updatedMatch = matchRepository.save(match);

        log.info("Match {} statut mis à jour: {}", matchId, newStatus);

        Pitch pitch = pitchRepository.findById(match.getPitchId()).orElse(null);
        return convertToResponse(updatedMatch, pitch);
    }

    public void cancelMatch(String matchId, String userId) {
        updateMatchStatus(matchId, MatchStatus.CANCELLED, userId);
    }

    public MatchResponse confirmMatch(String matchId, String userId) {
        return updateMatchStatus(matchId, MatchStatus.CONFIRMED, userId);
    }

    public List<MatchResponse> getMatchesByStatus(MatchStatus status) {
        List<Match> matches = matchRepository.findByStatus(status);

        return matches.stream()
                .map(match -> {
                    Pitch pitch = pitchRepository.findById(match.getPitchId())
                            .orElse(null);
                    return convertToResponse(match, pitch);
                })
                .collect(Collectors.toList());
    }

    private MatchResponse convertToResponse(Match match, Pitch pitch) {
        MatchResponse response = new MatchResponse();
        response.setId(match.getId());
        response.setSenderTeamId(match.getSenderTeamId());
        response.setSenderTeamName(match.getSenderTeamName());
        response.setSenderTeamLogoUrl(match.getSenderTeamLogoUrl());
        response.setReceiverTeamId(match.getReceiverTeamId());
        response.setReceiverTeamName(match.getReceiverTeamName());
        response.setReceiverTeamLogoUrl(match.getReceiverTeamLogoUrl());
        response.setMatchDate(match.getMatchDate());
        response.setMatchTime(match.getMatchTime());
        response.setStatus(match.getStatus());
        response.setCreatedAt(match.getCreatedAt());

        if (match.getScore() != null) {
            response.setHomeScore(match.getScore().getHome());
            response.setAwayScore(match.getScore().getAway());
        }

        if (pitch != null) {
            PitchDTO pitchDTO = new PitchDTO();
            pitchDTO.setId(pitch.getId());
            pitchDTO.setName(pitch.getName());
            pitchDTO.setAddress(pitch.getAddress());
            pitchDTO.setCity(pitch.getCity());
            pitchDTO.setPrice(pitch.getPrice());
            pitchDTO.setSize(pitch.getSize());
            pitchDTO.setRating(pitch.getRating());
            pitchDTO.setSurfaceType(pitch.getSurfaceType());
            pitchDTO.setImageUrl(pitch.getImageUrl());
            pitchDTO.setActive(pitch.isActive());
            response.setPitch(pitchDTO);
        }

        return response;
    }
}