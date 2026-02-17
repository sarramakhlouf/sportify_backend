package com.app.sportify_backend.repositories;

import com.app.sportify_backend.models.Formation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FormationRepository extends MongoRepository<Formation, String> {
    Optional<Formation> findByTeamId(String teamId);
}
