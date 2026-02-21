package com.app.sportify_backend.services;

import com.app.sportify_backend.dto.ReservationResponse;
import com.app.sportify_backend.models.Pitch;
import com.app.sportify_backend.models.Reservation;
import com.app.sportify_backend.models.ReservationStatus;
import com.app.sportify_backend.repositories.PitchRepository;
import com.app.sportify_backend.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class PitchService {

    private final PitchRepository pitchRepository;
    private final ReservationRepository reservationRepository;

    public Pitch createPitch(Pitch pitch, String managerId, MultipartFile image) throws IOException {
        pitch.setCreatedBy(managerId);
        pitch.setCreatedViaValidation(true);
        pitch.setCreatedViaBackoffice(false);
        pitch.setCreatedAt(LocalDateTime.now());

        pitch = pitchRepository.save(pitch);

        if (image != null && !image.isEmpty()) {
            String fileExtension = image.getOriginalFilename()
                    .substring(image.getOriginalFilename().lastIndexOf("."));
            String filename = pitch.getId() + "_" + UUID.randomUUID() + fileExtension;

            Path uploadPath = Paths.get("uploads/pitches/" + filename);
            Files.createDirectories(uploadPath.getParent());
            Files.write(uploadPath, image.getBytes());

            pitch.setImageUrl("/uploads/pitches/" + filename);
            pitch = pitchRepository.save(pitch);
        }

        log.info("Pitch créé dans la collection: {} pour le manager {}", pitch.getId(), managerId);

        return pitch;
    }

    public Pitch getManagerPitch(String managerId) {
        return pitchRepository.findByCreatedBy(managerId)
                .orElse(null);
    }

    public List<Pitch> getAllPitches() {
        return pitchRepository.findAll();
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

    public void deactivatePitch(String pitchId) {
        pitchRepository.findById(pitchId).ifPresent(pitch -> {
            pitch.setActive(false);
            pitchRepository.save(pitch);
            log.info("Pitch {} désactivé", pitch.getId());
        });
    }

    public void activatePitch(String pitchId) {
        pitchRepository.findById(pitchId).ifPresent(pitch -> {
            pitch.setActive(true);
            pitchRepository.save(pitch);
            log.info("Pitch {} activé", pitch.getId());
        });
    }

    public void deletePitch(String pitchId) {
        pitchRepository.findById(pitchId).ifPresent(pitch -> {
            pitchRepository.delete(pitch);
            log.info("Pitch {} supprimé", pitch.getId());
        });
    }

    public void deactivateManagerPitch(String managerId) {
        pitchRepository.findByCreatedBy(managerId).ifPresent(pitch -> {
            pitch.setActive(false);
            pitchRepository.save(pitch);
        });
    }

    public void deleteManagerPitch(String managerId) {
        pitchRepository.findByCreatedBy(managerId).ifPresent(pitch -> {
            pitchRepository.delete(pitch);
        });
    }

    public List<Pitch> getPitchesByCity(String city) {
        return pitchRepository.findByCity(city);
    }

    public Pitch getPitchById(String pitchId) {
        return pitchRepository.findById(pitchId)
                .orElseThrow(() -> new RuntimeException("Pitch non trouvé avec l'ID: " + pitchId));
    }

    public List<Pitch> searchPitchesByName(String name) {
        log.info("Recherche de terrains avec le nom contenant: {}", name);
        return pitchRepository.findAll().stream()
                .filter(pitch -> pitch.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Pitch> searchPitchesByCity(String city) {
        log.info("Recherche de terrains dans la ville: {}", city);
        return pitchRepository.findAll().stream()
                .filter(pitch -> pitch.getCity().toLowerCase().contains(city.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Pitch> searchPitches(String query) {
        log.info("Recherche globale avec le terme: {}", query);
        String lowerQuery = query.toLowerCase();

        return pitchRepository.findAll().stream()
                .filter(pitch ->
                        pitch.getName().toLowerCase().contains(lowerQuery) ||
                                pitch.getCity().toLowerCase().contains(lowerQuery) ||
                                (pitch.getAddress() != null && pitch.getAddress().toLowerCase().contains(lowerQuery))
                )
                .collect(Collectors.toList());
    }

    //---------------------GET TODAY MATCH COUNT--------------------------------------------------------------------
    public long getTodayMatchesCount(String pitchId, String userId) {
        Pitch pitch = pitchRepository.findById(pitchId)
                .orElseThrow(() -> new RuntimeException("PITCH_NOT_FOUND"));

        if (!pitch.getCreatedBy().equals(userId)) {
            throw new RuntimeException("NOT_PITCH_OWNER");
        }

        return pitchRepository.countByPitchIdAndStatusAndDay(
                pitchId,
                ReservationStatus.CONFIRMED,
                LocalDate.now()
        );
    }

    //---------------------GET ALL RESERVATIONS FOR A PITCH (public - pour voir les créneaux disponibles)----------
    public List<ReservationResponse> getConfirmedPitchReservations(String pitchId) {

        Pitch pitch = pitchRepository.findById(pitchId)
                .orElseThrow(() -> new RuntimeException("PITCH_NOT_FOUND"));

        List<Reservation> reservations = reservationRepository
                .findByPitchIdAndStatusOrderByDayAscHourAsc(
                        pitchId,
                        ReservationStatus.CONFIRMED
                );

        return reservations.stream()

                .map(ReservationResponse::from)
                .collect(Collectors.toList());
    }

    //---------------------GET AVAILABLE TIME SLOTS------------------------------------------------------------------
    public List<Map<String, Object>> getAvailableTimeSlots(
            String pitchId,
            LocalDate day
    ) {

        Pitch pitch = pitchRepository.findById(pitchId)
                .orElseThrow(() -> new RuntimeException("PITCH_NOT_FOUND"));

        List<Reservation> reservations = reservationRepository
                .findByPitchIdAndDay(pitchId, day);

        List<Map<String, Object>> timeSlots = new ArrayList<>();
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(22, 0);

        while (startTime.isBefore(endTime)) {
            LocalTime currentTime = startTime;

            boolean isAvailable = reservations.stream()
                    .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                    .noneMatch(r -> r.getHour().equals(currentTime));

            Map<String, Object> slot = new HashMap<>();
            slot.put("time", currentTime.toString());
            slot.put("available", isAvailable);

            timeSlots.add(slot);

            startTime = startTime.plusHours(1);
        }

        return timeSlots;
    }

    //---------------------GET WEEKLY STATS------------------------------------------------------------------
    public Map<String, Long> getWeeklyStats(String pitchId, String userId) {
        Pitch pitch = pitchRepository.findById(pitchId)
                .orElseThrow(() -> new RuntimeException("PITCH_NOT_FOUND"));

        if (!pitch.getCreatedBy().equals(userId)) {
            throw new RuntimeException("NOT_PITCH_OWNER");
        }

        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);

        Map<String, Long> stats = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            long count = pitchRepository.countByPitchIdAndStatusAndDay(
                    pitchId, ReservationStatus.CONFIRMED, day
            );
            stats.put(day.getDayOfWeek().name(), count); // "MONDAY", "TUESDAY"...
        }

        return stats;
    }
}