package com.app.sportify_backend.controllers;

import com.app.sportify_backend.models.Pitch;
import com.app.sportify_backend.services.PitchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

}