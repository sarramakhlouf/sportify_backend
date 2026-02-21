package com.app.sportify_backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "team_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamStats {

    @Id
    private String id;

    private String teamId;
    private int played;
    private int wins;
    private int draws;
    private int losses;
    private int goalsScored;
    private int goalsConceded;
    private int goalDifference;
    private double winRate;
    private LocalDateTime updatedAt;
}