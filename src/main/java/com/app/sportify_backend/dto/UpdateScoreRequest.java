package com.app.sportify_backend.dto;

import lombok.Data;

@Data
public class UpdateScoreRequest {
    private Integer homeScore;
    private Integer awayScore;
}