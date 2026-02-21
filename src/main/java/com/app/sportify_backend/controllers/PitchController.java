package com.app.sportify_backend.controllers;

import com.app.sportify_backend.dto.ReservationResponse;
import com.app.sportify_backend.models.Pitch;
import com.app.sportify_backend.models.User;
import com.app.sportify_backend.services.PitchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pitches")
@RequiredArgsConstructor
public class PitchController {

    private final PitchService pitchService;

    @GetMapping
    public ResponseEntity<List<Pitch>> getAllPitches() {
        List<Pitch> pitches = pitchService.getAllPitches();
        return ResponseEntity.ok(pitches);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Pitch>> getAllActivePitches() {
        List<Pitch> pitches = pitchService.getAllActivePitches();
        return ResponseEntity.ok(pitches);
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<Pitch>> getPitchesByCity(@PathVariable String city) {
        List<Pitch> pitches = pitchService.getPitchesByCity(city);
        return ResponseEntity.ok(pitches);
    }

    @GetMapping("/{pitchId}")
    public ResponseEntity<Pitch> getPitchById(@PathVariable String pitchId) {
        Pitch pitch = pitchService.getPitchById(pitchId);
        if (pitch == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pitch);
    }

    @PutMapping("/{pitchId}")
    public ResponseEntity<Pitch> updatePitch(
            @PathVariable String pitchId,
            @RequestBody Pitch updatedPitch) {
        try {
            Pitch pitch = pitchService.updatePitch(pitchId, updatedPitch);
            return ResponseEntity.ok(pitch);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{pitchId}")
    public ResponseEntity<Void> deletePitch(@PathVariable String pitchId) {
        try {
            pitchService.deletePitch(pitchId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{pitchId}/activate")
    public ResponseEntity<Void> activatePitch(@PathVariable String pitchId) {
        try {
            pitchService.activatePitch(pitchId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{pitchId}/deactivate")
    public ResponseEntity<Void> deactivatePitch(@PathVariable String pitchId) {
        try {
            pitchService.deactivatePitch(pitchId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Pitch>> searchPitches(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city
    ) {
        if (query != null && !query.isEmpty()) {
            return ResponseEntity.ok(pitchService.searchPitches(query));
        } else if (name != null && !name.isEmpty()) {
            return ResponseEntity.ok(pitchService.searchPitchesByName(name));
        } else if (city != null && !city.isEmpty()) {
            return ResponseEntity.ok(pitchService.searchPitchesByCity(city));
        }
        return ResponseEntity.ok(pitchService.getAllPitches());
    }

    @GetMapping("/pitch/{pitchId}/today-count")
    public ResponseEntity<Map<String, Long>> getTodayMatchesCount(
            @PathVariable String pitchId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        long count = pitchService.getTodayMatchesCount(pitchId, user.getId() );
        return ResponseEntity.ok(Map.of("todayMatchesCount", count));
    }

    @GetMapping("/pitch/{pitchId}/confirmed")
    public ResponseEntity<List<ReservationResponse>> getConfirmedPitchReservations(
            @PathVariable String pitchId
    ) {
        List<ReservationResponse> reservations = pitchService.getConfirmedPitchReservations(pitchId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/pitch/{pitchId}/available-slots")
    public ResponseEntity<List<Map<String, Object>>> getAvailableTimeSlots(
            @PathVariable String pitchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day
    ) {
        List<Map<String, Object>> slots = pitchService.getAvailableTimeSlots(pitchId, day);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/{pitchId}/weekly-stats")
    public ResponseEntity<Map<String, Long>> getWeeklyStats(
            @PathVariable String pitchId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(pitchService.getWeeklyStats(pitchId, user.getId()));
    }
}