package com.app.sportify_backend.dto;

import com.app.sportify_backend.models.CancelReason;
import com.app.sportify_backend.models.Reservation;
import com.app.sportify_backend.models.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationResponse {

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
    private Integer duration;

    // Équipes
    private String senderTeamId;
    private String senderTeamName;
    private String senderTeamLogoUrl;

    private String adverseTeamId;
    private String adverseTeamName;
    private String adverseTeamLogoUrl;

    // Utilisateurs
    private String senderId;
    private String receiverId;

    // Statut
    private ReservationStatus status;

    // Annulation
    private boolean cancelledBySender;
    private boolean cancelledByReceiver;
    private CancelReason cancelReason;

    // Score du match
    private Reservation.Score score;

    // Dates
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReservationResponse from(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .pitchId(reservation.getPitchId())
                .pitchName(reservation.getPitchName())
                .pitchAddress(reservation.getPitchAddress())
                .pitchImageUrl(reservation.getPitchImageUrl())
                .pitchPrice(reservation.getPitchPrice())
                .day(reservation.getDay())
                .hour(reservation.getHour())
                .duration(reservation.getDuration())
                .senderTeamId(reservation.getSenderTeamId())
                .senderTeamName(reservation.getSenderTeamName())
                .senderTeamLogoUrl(reservation.getSenderTeamLogoUrl())
                .adverseTeamId(reservation.getAdverseTeamId())
                .adverseTeamName(reservation.getAdverseTeamName())
                .adverseTeamLogoUrl(reservation.getAdverseTeamLogoUrl())
                .senderId(reservation.getSenderId())
                .receiverId(reservation.getReceiverId())
                .status(reservation.getStatus())
                .cancelledBySender(reservation.isCancelledBySender())
                .cancelledByReceiver(reservation.isCancelledByReceiver())
                .score(reservation.getScore())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
}