package com.app.sportify_backend.repositories;

import com.app.sportify_backend.models.TeamStats;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TeamStatsRepository extends MongoRepository<TeamStats, String> {
    Optional<TeamStats> findByTeamId(String teamId);
}
