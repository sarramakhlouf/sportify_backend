package com.app.sportify_backend.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateMatchRequest {
    private String senderTeamId;
    private String senderTeamName;
    private String receiverTeamId;
    private String receiverTeamName;
    private String pitchId;
    private LocalDate matchDate;
    private LocalTime matchTime;
}