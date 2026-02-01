package com.app.sportify_backend.dto;

import com.app.sportify_backend.models.Team;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlayerTeamsResponse {
    private List<Team> ownedTeams;
    private List<Team> memberTeams;
}
