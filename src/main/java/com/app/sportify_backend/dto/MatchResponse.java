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
    private String receiverTeamId;
    private String receiverTeamName;
    private PitchDTO pitch;
    private LocalDate matchDate;
    private LocalTime matchTime;
    private MatchStatus status;
    private LocalDateTime createdAt;
}