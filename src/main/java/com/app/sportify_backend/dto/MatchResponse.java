package com.app.sportify_backend.dto;

import com.app.sportify_backend.models.MatchStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class MatchResponse {
    private String id;

    private String senderTeamId;
    private String senderTeamName;
    private String senderTeamLogoUrl;

    private String receiverTeamId;
    private String receiverTeamName;
    private String receiverTeamLogoUrl;

    private PitchDTO pitch;

    private LocalDate matchDate;
    private LocalTime matchTime;

    private MatchStatus status;

    private Integer homeScore;
    private Integer awayScore;

    private LocalDateTime createdAt;
}