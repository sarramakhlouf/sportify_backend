package com.app.sportify_backend.services;

import com.app.sportify_backend.models.Pitch;
import com.app.sportify_backend.repositories.PitchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PitchService {

    private final PitchRepository pitchRepository;

    public Pitch createPitch(Pitch pitch, String managerId) {
        pitch.setCreatedBy(managerId);
        pitch.setCreatedViaValidation(true);
        pitch.setCreatedViaBackoffice(false);
        pitch.setActive(true);
        pitch.setCreatedAt(LocalDateTime.now());

        Pitch savedPitch = pitchRepository.save(pitch);
        log.info("Pitch créé dans la collection: {} pour le manager {}", savedPitch.getId(), managerId);

        return savedPitch;
    }

    public Pitch getManagerPitch(String managerId) {
        return pitchRepository.findByCreatedBy(managerId)
                .orElse(null);
    }

    public List<Pitch> getAllActivePitches() {
        return pitchRepository.findByIsActiveTrue();
    }

    public Pitch updatePitch(String pitchId, Pitch updatedPitch) {
        Pitch pitch = pitchRepository.findById(pitchId)
                .orElseThrow(() -> new RuntimeException("Pitch non trouvé"));

        pitch.setName(updatedPitch.getName());
        pitch.setAddress(updatedPitch.getAddress());
        pitch.setCity(updatedPitch.getCity());
        pitch.setPrice(updatedPitch.getPrice());
        pitch.setSize(updatedPitch.getSize());
        pitch.setSurfaceType(updatedPitch.getSurfaceType());
        pitch.setImageUrl(updatedPitch.getImageUrl());

        return pitchRepository.save(pitch);
    }

    public void deactivateManagerPitch(String managerId) {
        pitchRepository.findByCreatedBy(managerId).ifPresent(pitch -> {
            pitch.setActive(false);
            pitchRepository.save(pitch);
            log.info("Pitch {} désactivé", pitch.getId());
        });
    }

    public void activateManagerPitch(String managerId) {
        pitchRepository.findByCreatedBy(managerId).ifPresent(pitch -> {
            pitch.setActive(true);
            pitchRepository.save(pitch);
            log.info("Pitch {} activé", pitch.getId());
        });
    }

    public void deleteManagerPitch(String managerId) {
        pitchRepository.findByCreatedBy(managerId).ifPresent(pitch -> {
            pitchRepository.delete(pitch);
            log.info("Pitch {} supprimé", pitch.getId());
        });
    }

    public List<Pitch> getPitchesByCity(String city) {
        return pitchRepository.findByCityAndIsActiveTrue(city);
    }
}
