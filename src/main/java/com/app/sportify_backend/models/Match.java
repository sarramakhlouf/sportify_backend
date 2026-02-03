package com.app.sportify_backend.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Document(collection = "matches")
public class Match {
    @Id
    private String id;
    private String senderTeamId;
    private String senderTeamName;
    private String receiverTeamId;
    private String receiverTeamName;
    private String pitchId;
    private LocalDate matchDate;
    private LocalTime matchTime;
    private MatchStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}