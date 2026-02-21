package com.app.sportify_backend.dto;

import com.app.sportify_backend.models.TeamStats;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamStatsResponse {
    private String teamId;
    private int played;
    private int wins;
    private int draws;
    private int losses;
    private int goalsScored;
    private int goalsConceded;
    private int goalDifference;
    private double winRate;

    public static TeamStatsResponse from(TeamStats stats) {
        return TeamStatsResponse.builder()
                .teamId(stats.getTeamId())
                .played(stats.getPlayed())
                .wins(stats.getWins())
                .draws(stats.getDraws())
                .losses(stats.getLosses())
                .goalsScored(stats.getGoalsScored())
                .goalsConceded(stats.getGoalsConceded())
                .goalDifference(stats.getGoalDifference())
                .winRate(stats.getWinRate())
                .build();
    }
}
