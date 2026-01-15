package com.app.sportify_backend.repositories;

import com.app.sportify_backend.models.Team;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface TeamRepository extends MongoRepository<Team, String> {
    List<Team> findByOwnerId(String ownerId);
    Optional<Team> findByTeamCode(String teamCode);
    List<Team> findByMembersUserId(String userId);
}
