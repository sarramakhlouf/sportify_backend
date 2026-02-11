package com.app.sportify_backend.controllers;

import com.app.sportify_backend.dto.CreateReservationRequest;
import com.app.sportify_backend.dto.ReservationResponse;
import com.app.sportify_backend.dto.UpdateScoreRequest;
import com.app.sportify_backend.models.User;
import com.app.sportify_backend.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/create")
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody CreateReservationRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        ReservationResponse response = reservationService.createReservation(
                request.getSenderTeamId(),
                request.getAdverseTeamId(),
                request.getPitchId(),
                request.getDay(),
                request.getHour(),
                request.getDuration(),
                user.getId()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{reservationId}/confirm")
    public ResponseEntity<Void> confirmReservation(
            @PathVariable String reservationId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        reservationService.confirmReservation(reservationId, user.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{reservationId}/reject")
    public ResponseEntity<Void> rejectReservation(
            @PathVariable String reservationId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        reservationService.rejectReservation(reservationId, user.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable String reservationId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        reservationService.cancelReservation(reservationId, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ReservationResponse>> getPendingReservations(
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        List<ReservationResponse> reservations = reservationService.getPendingReservations(user.getId());
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/my-teams")
    public ResponseEntity<List<ReservationResponse>> getMyTeamReservations(
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        List<ReservationResponse> reservations = reservationService.getMyTeamReservations(user.getId());
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/pitch/{pitchId}")
    public ResponseEntity<List<ReservationResponse>> getPitchReservations(
            @PathVariable String pitchId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        List<ReservationResponse> reservations = reservationService.getPitchReservations(pitchId, user.getId());
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/pitch/{pitchId}/confirmed")
    public ResponseEntity<List<ReservationResponse>> getConfirmedPitchReservations(
            @PathVariable String pitchId
    ) {
        List<ReservationResponse> reservations = reservationService.getConfirmedPitchReservations(pitchId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/pitch/{pitchId}/available-slots")
    public ResponseEntity<List<Map<String, Object>>> getAvailableTimeSlots(
            @PathVariable String pitchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day
    ) {
        List<Map<String, Object>> slots = reservationService.getAvailableTimeSlots(pitchId, day);
        return ResponseEntity.ok(slots);
    }

    @PutMapping("/{reservationId}/score")
    public ResponseEntity<ReservationResponse> updateScore(
            @PathVariable String reservationId,
            Authentication authentication,
            @RequestBody UpdateScoreRequest request
    ) {
        User user = (User) authentication.getPrincipal();
        ReservationResponse response = reservationService.updateScore(
                reservationId,
                request.getHomeScore(),
                request.getAwayScore(),
                user.getId()
        );
        return ResponseEntity.ok(response);
    }

    // -------------------- NOUVEAUX ENDPOINTS --------------------

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<ReservationResponse>> getTeamReservations(
            @PathVariable String teamId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        List<ReservationResponse> reservations = reservationService.getTeamReservations(teamId, user.getId());
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> getReservationById(
            @PathVariable String reservationId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        ReservationResponse reservation = reservationService.getReservationById(reservationId, user.getId());
        return ResponseEntity.ok(reservation);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReservationResponse>> getReservationsByStatus(
            @PathVariable String status,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        List<ReservationResponse> reservations = reservationService.getReservationsByStatus(status, user.getId());
        return ResponseEntity.ok(reservations);
    }

    @PutMapping("/{reservationId}/status")
    public ResponseEntity<ReservationResponse> updateReservationStatus(
            @PathVariable String reservationId,
            @RequestParam String status,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        ReservationResponse response = reservationService.updateReservationStatus(
                reservationId,
                status,
                user.getId()
        );
        return ResponseEntity.ok(response);
    }
}