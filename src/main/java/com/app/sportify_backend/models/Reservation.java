package com.app.sportify_backend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Document(collection = "reservations")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Reservation {

    @Id
    private String id;

    // Informations du terrain
    private String pitchId;
    private String pitchName;
    private String pitchAddress;
    private String pitchImageUrl;
    private Double pitchPrice;

    // Informations de réservation
    private LocalDate day;
    private LocalTime hour;
    private Integer duration; // durée en minutes

    // Équipes
    private String senderTeamId;
    private String senderTeamName;
    private String senderTeamLogoUrl;

    private String adverseTeamId;
    private String adverseTeamName;
    private String adverseTeamLogoUrl;

    // Utilisateurs
    private String senderId; // owner de l'équipe sender
    private String receiverId; // createdBy du pitch

    // Statut
    private ReservationStatus status;

    // Annulation
    @Builder.Default
    private boolean cancelledBySender = false;

    @Builder.Default
    private boolean cancelledByReceiver = false;

    //private CancelReason cancelReason;

    private Score score;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Score {
        private Integer home;
        private Integer away;
    }
}