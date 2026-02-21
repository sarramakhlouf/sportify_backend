package com.app.sportify_backend.services;

import com.app.sportify_backend.dto.ReservationResponse;
import com.app.sportify_backend.dto.TeamStatsResponse;
import com.app.sportify_backend.exception.ResourceNotFoundException;
import com.app.sportify_backend.models.*;
import com.app.sportify_backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TeamRepository teamRepository;
    private final PitchRepository pitchRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final TeamStatsRepository teamStatsRepository;

    //---------------------CREATE RESERVATION------------------------------------------------------------------------
    @Transactional
    public ReservationResponse createReservation(
            String senderTeamId,
            String adverseTeamId,
            String pitchId,
            LocalDate day,
            LocalTime hour,
            Integer duration,
            String senderId
    ) {

        Team senderTeam = teamRepository.findById(senderTeamId)
                .orElseThrow(() -> new RuntimeException("SENDER_TEAM_NOT_FOUND"));

        if (!senderTeam.getOwnerId().equals(senderId)) {
            throw new RuntimeException("NOT_TEAM_OWNER");
        }

        Team adverseTeam = teamRepository.findById(adverseTeamId)
                .orElseThrow(() -> new RuntimeException("ADVERSE_TEAM_NOT_FOUND"));

        if (senderTeam.getId().equals(adverseTeam.getId())) {
            throw new RuntimeException("CANNOT_PLAY_AGAINST_OWN_TEAM");
        }

        Pitch pitch = pitchRepository.findById(pitchId)
                .orElseThrow(() -> new RuntimeException("PITCH_NOT_FOUND"));

        if (!pitch.isActive()) {
            throw new RuntimeException("PITCH_NOT_ACTIVE");
        }

        if (day.isBefore(LocalDate.now()) ||
                (day.isEqual(LocalDate.now()) && hour.isBefore(LocalTime.now()))) {
            throw new RuntimeException("INVALID_DATE_TIME");
        }

        Optional<Reservation> existingReservation = reservationRepository
                .findByPitchIdAndDayAndHourAndStatus(
                        pitchId,
                        day,
                        hour,
                        ReservationStatus.CONFIRMED
                );

        if (existingReservation.isPresent()) {
            throw new RuntimeException("TIME_SLOT_ALREADY_BOOKED");
        }

        User pitchOwner = userRepository.findById(pitch.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("PITCH_OWNER_NOT_FOUND"));

        Reservation reservation = Reservation.builder()
                .pitchId(pitch.getId())
                .pitchName(pitch.getName())
                .pitchAddress(pitch.getAddress())
                .pitchImageUrl(pitch.getImageUrl())
                .pitchPrice(pitch.getPrice())
                .day(day)
                .hour(hour)
                .duration(duration)
                .senderTeamId(senderTeam.getId())
                .senderTeamName(senderTeam.getName())
                .senderTeamLogoUrl(senderTeam.getLogoUrl())
                .adverseTeamId(adverseTeam.getId())
                .adverseTeamName(adverseTeam.getName())
                .adverseTeamLogoUrl(adverseTeam.getLogoUrl())
                .senderId(senderId)
                .receiverId(pitch.getCreatedBy())
                .status(ReservationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        reservationRepository.save(reservation);

        Map<String, Object> data = new HashMap<>();
        data.put("reservationId", reservation.getId());
        data.put("pitchId", pitch.getId());
        data.put("pitchName", pitch.getName());
        data.put("senderTeamName", senderTeam.getName());
        data.put("adverseTeamName", adverseTeam.getName());
        data.put("day", day.toString());
        data.put("hour", hour.toString());

        notificationService.send(
                pitch.getCreatedBy(),
                senderId,
                "Nouvelle demande de réservation",
                senderTeam.getName() + " souhaite réserver " + pitch.getName() +
                        " le " + day + " à " + hour,
                NotificationType.RESERVATION_REQUEST,
                pitch.getId(),
                data
        );

        return ReservationResponse.from(reservation);
    }

    //---------------------CONFIRM RESERVATION--------------------------------------
    @Transactional
    public void confirmReservation(String reservationId, String userId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("RESERVATION_NOT_FOUND"));

        if (!reservation.getReceiverId().equals(userId)) {
            throw new RuntimeException("NOT_PITCH_OWNER");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("RESERVATION_ALREADY_PROCESSED");
        }

        Optional<Reservation> conflictingReservation = reservationRepository
                .findByPitchIdAndDayAndHourAndStatus(
                        reservation.getPitchId(),
                        reservation.getDay(),
                        reservation.getHour(),
                        ReservationStatus.CONFIRMED
                );

        if (conflictingReservation.isPresent() &&
                !conflictingReservation.get().getId().equals(reservationId)) {
            throw new RuntimeException("TIME_SLOT_ALREADY_BOOKED");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        Team senderTeam = teamRepository.findById(reservation.getSenderTeamId())
                .orElseThrow(() -> new RuntimeException("TEAM_NOT_FOUND"));

        Map<String, Object> data = new HashMap<>();
        data.put("reservationId", reservation.getId());
        data.put("pitchId", reservation.getPitchId());
        data.put("pitchName", reservation.getPitchName());
        data.put("day", reservation.getDay().toString());
        data.put("hour", reservation.getHour().toString());

        notificationService.send(
                userId,
                reservation.getSenderId(),
                "Réservation confirmée",
                "Votre réservation de " + reservation.getPitchName() +
                        " a été confirmée pour le " + reservation.getDay() + " à " + reservation.getHour(),
                NotificationType.RESERVATION_CONFIRMED,
                reservation.getPitchId(),
                data
        );
    }

    //---------------------REJECT RESERVATION---------------------------------------
    @Transactional
    public void rejectReservation(String reservationId, String userId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("RESERVATION_NOT_FOUND"));


        if (!reservation.getReceiverId().equals(userId)) {
            throw new RuntimeException("NOT_PITCH_OWNER");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("RESERVATION_ALREADY_PROCESSED");
        }

        reservation.setStatus(ReservationStatus.REJECTED);
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        Map<String, Object> data = new HashMap<>();
        data.put("reservationId", reservation.getId());
        data.put("pitchId", reservation.getPitchId());
        data.put("pitchName", reservation.getPitchName());
        data.put("day", reservation.getDay().toString());
        data.put("hour", reservation.getHour().toString());

        notificationService.send(
                userId,
                reservation.getSenderId(),
                "Réservation refusée",
                "Votre réservation de " + reservation.getPitchName() +
                        " a été refusée",
                NotificationType.RESERVATION_REJECTED,
                reservation.getPitchId(),
                data
        );
    }

    //---------------------CANCEL RESERVATION------------------------------------------------------------------------
    @Transactional
    public void cancelReservation(
            String reservationId,
            String userId
    ) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("RESERVATION_NOT_FOUND"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED ||
                reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new RuntimeException("RESERVATION_ALREADY_PROCESSED");
        }

        boolean isSender = reservation.getSenderId().equals(userId);
        boolean isReceiver = reservation.getReceiverId().equals(userId);

        if (!isSender && !isReceiver) {
            throw new RuntimeException("NOT_ALLOWED");
        }

        if (isSender) {
            reservation.setCancelledBySender(true);
        }

        if (isReceiver) {
            reservation.setCancelledByReceiver(true);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        String notificationReceiverId = isSender ? reservation.getReceiverId() : reservation.getSenderId();
        String cancellerName = isSender ? reservation.getSenderTeamName() : "Le propriétaire";

        Map<String, Object> data = new HashMap<>();
        data.put("reservationId", reservation.getId());
        data.put("pitchId", reservation.getPitchId());
        data.put("pitchName", reservation.getPitchName());
        data.put("day", reservation.getDay().toString());
        data.put("hour", reservation.getHour().toString());

        notificationService.send(
                userId,
                notificationReceiverId,
                "Réservation annulée",
                cancellerName + " a annulé la réservation de " + reservation.getPitchName() +
                        " prévue le " + reservation.getDay() + " à " + reservation.getHour(),
                NotificationType.RESERVATION_CANCELLED,
                reservation.getPitchId(),
                data
        );
    }

    //---------------------GET PENDING RESERVATIONS FOR PITCH MANAGER--------------------
    public List<ReservationResponse> getPendingReservations(String pitchId, String userId) {

        Pitch pitch = pitchRepository.findById(pitchId)
                .orElseThrow(() -> new RuntimeException("PITCH_NOT_FOUND"));

        if (!pitch.getCreatedBy().equals(userId)) {
            throw new RuntimeException("NOT_PITCH_OWNER");
        }

        List<Reservation> reservations = reservationRepository
                .findByPitchIdAndStatusOrderByDayAscHourAsc(
                        pitchId,
                        ReservationStatus.PENDING
                );

        return reservations.stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
    }

    //---------------------GET TEAM RESERVATIONS------------------------------------------------------------------
    public List<ReservationResponse> getTeamReservations(String teamId, String userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Équipe non trouvée"));

        List<Reservation> reservations = new ArrayList<>();

        List<Reservation> senderReservations = reservationRepository
                .findBySenderTeamIdAndCancelledBySenderFalse(teamId);
        reservations.addAll(senderReservations);

        List<Reservation> adverseReservations = reservationRepository
                .findByAdverseTeamId(teamId);
        reservations.addAll(adverseReservations);

        Map<String, Reservation> uniqueReservations = reservations.stream()
                .collect(Collectors.toMap(
                        Reservation::getId,
                        r -> r,
                        (existing, replacement) -> existing
                ));

        System.out.println("Reservations for team " + teamId + ": " + uniqueReservations.size());
        uniqueReservations.values().forEach(r ->
                System.out.println("   - Reservation " + r.getId() + ": " + r.getStatus())
        );

        return uniqueReservations.values().stream()
                .map(ReservationResponse::from)
                .sorted(Comparator.comparing(ReservationResponse::getDay)
                        .thenComparing(ReservationResponse::getHour))
                .collect(Collectors.toList());
    }

    //---------------------UPDATE SCORE------------------------------------------------------------------------------
    @Transactional
    public ReservationResponse updateScore(
            String reservationId,
            Integer homeScore,
            Integer awayScore,
            String userId
    ) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("RESERVATION_NOT_FOUND"));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED &&
                reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new RuntimeException("RESERVATION_NOT_CONFIRMED");
        }

        boolean isSender = reservation.getSenderId().equals(userId);
        boolean isReceiver = reservation.getReceiverId().equals(userId);

        Team senderTeam = teamRepository.findById(reservation.getSenderTeamId()).orElse(null);
        Team adverseTeam = teamRepository.findById(reservation.getAdverseTeamId()).orElse(null);

        boolean isSenderTeamMember = senderTeam != null &&
                senderTeam.getMembers().stream().anyMatch(m -> m.getUserId().equals(userId));
        boolean isAdverseTeamMember = adverseTeam != null &&
                adverseTeam.getMembers().stream().anyMatch(m -> m.getUserId().equals(userId));

        if (!isSender && !isReceiver && !isSenderTeamMember && !isAdverseTeamMember) {
            throw new RuntimeException("NOT_ALLOWED");
        }

        Reservation.Score score = Reservation.Score.builder()
                .home(homeScore)
                .away(awayScore)
                .build();

        reservation.setScore(score);

        LocalDateTime matchDateTime = LocalDateTime.of(reservation.getDay(), reservation.getHour());
        if (LocalDateTime.now().isAfter(matchDateTime.plusMinutes(reservation.getDuration()))) {
            reservation.setStatus(ReservationStatus.COMPLETED);
        }

        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            notifyScoreUpdate(reservation);
            recalculateAndSaveTeamStats(reservation.getSenderTeamId());
            recalculateAndSaveTeamStats(reservation.getAdverseTeamId());
        }

        return ReservationResponse.from(reservation);
    }

    //---------------------NOTIFY SCORE UPDATE-----------------------------------------------------------------------
    private void notifyScoreUpdate(Reservation reservation) {

        Team senderTeam = teamRepository.findById(reservation.getSenderTeamId())
                .orElseThrow(() -> new RuntimeException("SENDER_TEAM_NOT_FOUND"));

        Team adverseTeam = teamRepository.findById(reservation.getAdverseTeamId())
                .orElseThrow(() -> new RuntimeException("ADVERSE_TEAM_NOT_FOUND"));

        Map<String, Object> data = new HashMap<>();
        data.put("reservationId", reservation.getId());
        data.put("pitchId", reservation.getPitchId());
        data.put("pitchName", reservation.getPitchName());
        data.put("senderTeamName", senderTeam.getName());
        data.put("adverseTeamName", adverseTeam.getName());
        data.put("scoreHome", reservation.getScore().getHome());
        data.put("scoreAway", reservation.getScore().getAway());
        data.put("day", reservation.getDay().toString());
        data.put("hour", reservation.getHour().toString());

        String scoreText = senderTeam.getName() + " " +
                reservation.getScore().getHome() + " - " +
                reservation.getScore().getAway() + " " +
                adverseTeam.getName();

        for (Team.TeamMember member : senderTeam.getMembers()) {
            if (!member.getUserId().equals(reservation.getSenderId()) &&
                    !member.getUserId().equals(senderTeam.getOwnerId())) {  // <-- ajout
                notificationService.send(
                        reservation.getSenderId(),
                        member.getUserId(),
                        "Score final enregistré",
                        "Le match contre " + adverseTeam.getName() + " est terminé: " + scoreText,
                        NotificationType.MATCH_COMPLETED,
                        senderTeam.getId(),
                        data
                );
            }
        }

        for (Team.TeamMember member : adverseTeam.getMembers()) {
            if (!member.getUserId().equals(adverseTeam.getOwnerId())) {  // <-- ajout
                notificationService.send(
                        reservation.getSenderId(),
                        member.getUserId(),
                        "Score final enregistré",
                        "Le match contre " + senderTeam.getName() + " est terminé: " + scoreText,
                        NotificationType.MATCH_COMPLETED,
                        adverseTeam.getId(),
                        data
                );
            }
        }
    }

    //---------------------GET RESERVATION BY ID---------------------------------------------------------------------
    public ReservationResponse getReservationById(String reservationId, String userId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("RESERVATION_NOT_FOUND"));

        boolean isSender = reservation.getSenderId().equals(userId);
        boolean isReceiver = reservation.getReceiverId().equals(userId);

        Team senderTeam = teamRepository.findById(reservation.getSenderTeamId()).orElse(null);
        Team adverseTeam = teamRepository.findById(reservation.getAdverseTeamId()).orElse(null);

        boolean isSenderTeamMember = senderTeam != null && (
                senderTeam.getOwnerId().equals(userId) ||
                        senderTeam.getMembers().stream().anyMatch(m -> m.getUserId().equals(userId))
        );

        boolean isAdverseTeamMember = adverseTeam != null && (
                adverseTeam.getOwnerId().equals(userId) ||
                        adverseTeam.getMembers().stream().anyMatch(m -> m.getUserId().equals(userId))
        );

        if (!isSender && !isReceiver && !isSenderTeamMember && !isAdverseTeamMember) {
            throw new RuntimeException("NOT_ALLOWED");
        }

        return ReservationResponse.from(reservation);
    }

    //---------------------GET RESERVATIONS BY STATUS----------------------------------------------------------------
    public List<ReservationResponse> getReservationsByStatus(String status, String userId) {

        ReservationStatus reservationStatus;
        try {
            reservationStatus = ReservationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("INVALID_STATUS");
        }

        List<Team> myTeams = teamRepository.findByOwnerId(userId);
        Set<String> myTeamIds = myTeams.stream()
                .map(Team::getId)
                .collect(Collectors.toSet());

        List<Reservation> senderReservations = reservationRepository
                .findByStatusAndSenderIdOrReceiverId(reservationStatus, userId, userId);

        List<Reservation> teamReservations = new ArrayList<>();
        for (String teamId : myTeamIds) {
            teamReservations.addAll(
                    reservationRepository.findBySenderTeamIdAndStatus(teamId, reservationStatus)
            );
        }

        Map<String, Reservation> uniqueReservations = new HashMap<>();

        for (Reservation r : senderReservations) {
            uniqueReservations.put(r.getId(), r);
        }

        for (Reservation r : teamReservations) {
            uniqueReservations.put(r.getId(), r);
        }

        return uniqueReservations.values().stream()
                .map(ReservationResponse::from)
                .sorted(Comparator.comparing(ReservationResponse::getDay)
                        .thenComparing(ReservationResponse::getHour))
                .collect(Collectors.toList());
    }

    //---------------------UPDATE RESERVATION STATUS-----------------------------------------------------------------
    @Transactional
    public ReservationResponse updateReservationStatus(
            String reservationId,
            String status,
            String userId
    ) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("RESERVATION_NOT_FOUND"));

        ReservationStatus newStatus;
        try {
            newStatus = ReservationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("INVALID_STATUS");
        }

        boolean isSender = reservation.getSenderId().equals(userId);
        boolean isReceiver = reservation.getReceiverId().equals(userId);

        if (!isSender && !isReceiver) {
            throw new RuntimeException("NOT_ALLOWED");
        }

        switch (newStatus) {
            case CONFIRMED:
                if (!isReceiver) {
                    throw new RuntimeException("ONLY_RECEIVER_CAN_CONFIRM");
                }
                break;

            case REJECTED:
                if (!isReceiver) {
                    throw new RuntimeException("ONLY_RECEIVER_CAN_REJECT");
                }
                break;

            case CANCELLED:
                // Les deux peuvent annuler
                if (isSender) {
                    reservation.setCancelledBySender(true);
                }
                if (isReceiver) {
                    reservation.setCancelledByReceiver(true);
                }
                break;

            case COMPLETED:
                LocalDateTime matchDateTime = LocalDateTime.of(
                        reservation.getDay(),
                        reservation.getHour()
                );
                if (LocalDateTime.now().isBefore(matchDateTime.plusMinutes(reservation.getDuration()))) {
                    throw new RuntimeException("MATCH_NOT_FINISHED_YET");
                }
                break;

            default:
                break;
        }

        reservation.setStatus(newStatus);
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        return ReservationResponse.from(reservation);
    }

    //---------------------GET TEAM STATS------------------------------------------------------------
    public TeamStatsResponse getTeamStats(String teamId) {

        teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("TEAM_NOT_FOUND"));

        TeamStats stats = teamStatsRepository.findByTeamId(teamId)
                .orElse(TeamStats.builder()
                        .teamId(teamId)
                        .played(0).wins(0).draws(0).losses(0)
                        .goalsScored(0).goalsConceded(0)
                        .goalDifference(0).winRate(0.0)
                        .build());

        return TeamStatsResponse.from(stats);
    }

    //---------------------RECALCULATE AND SAVE TEAM STATS-------------------------------------------
    private void recalculateAndSaveTeamStats(String teamId) {

        List<Reservation> completedMatches = reservationRepository
                .findAllCompletedByTeam(ReservationStatus.COMPLETED, teamId);

        int wins = 0, draws = 0, losses = 0;
        int goalsScored = 0, goalsConceded = 0;
        int played = 0;

        for (Reservation r : completedMatches) {
            if (r.getScore() == null) continue;

            played++;
            boolean isSender = r.getSenderTeamId().equals(teamId);

            int myGoals    = isSender ? r.getScore().getHome() : r.getScore().getAway();
            int theirGoals = isSender ? r.getScore().getAway() : r.getScore().getHome();

            goalsScored   += myGoals;
            goalsConceded += theirGoals;

            if (myGoals > theirGoals)       wins++;
            else if (myGoals == theirGoals) draws++;
            else                            losses++;
        }

        TeamStats stats = teamStatsRepository.findByTeamId(teamId)
                .orElse(TeamStats.builder().teamId(teamId).build());

        stats.setPlayed(played);
        stats.setWins(wins);
        stats.setDraws(draws);
        stats.setLosses(losses);
        stats.setGoalsScored(goalsScored);
        stats.setGoalsConceded(goalsConceded);
        stats.setGoalDifference(goalsScored - goalsConceded);
        stats.setWinRate(played == 0 ? 0.0 : (double) wins / played * 100);
        stats.setUpdatedAt(LocalDateTime.now());

        teamStatsRepository.save(stats);
    }
}