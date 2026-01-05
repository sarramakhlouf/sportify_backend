package com.app.sportify_backend.repositories;

import com.app.sportify_backend.models.Team;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TeamRepository extends MongoRepository<Team, String> {
    List<Team> findByOwnerId(String ownerId);
}
