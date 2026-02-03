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
        return ResponseEntity.ok(null);
    }
}