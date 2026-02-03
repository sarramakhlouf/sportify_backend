package com.app.sportify_backend.repositories;
import com.app.sportify_backend.models.Pitch;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PitchRepository extends MongoRepository<Pitch, String> {

    Optional<Pitch> findByCreatedBy(String managerId);
    List<Pitch> findByIsActiveTrue();
    List<Pitch> findByCity(String city);
    List<Pitch> findByCityAndIsActiveTrue(String city);
    boolean existsByCreatedBy(String managerId);
}