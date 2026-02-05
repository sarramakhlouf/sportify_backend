package com.app.sportify_backend.repositories;
import com.app.sportify_backend.models.Pitch;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PitchRepository extends MongoRepository<Pitch, String> {

    Optional<Pitch> findByCreatedBy(String managerId);
    List<Pitch> findByCity(String city);
    List<Pitch> findByIsActiveTrue();
    List<Pitch> findByCityAndIsActiveTrue(String city);

    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    List<Pitch> findByNameContainingIgnoreCase(String name);

    @Query("{'city': {$regex: ?0, $options: 'i'}}")
    List<Pitch> findByCityContainingIgnoreCase(String city);

    @Query("{$or: [{'name': {$regex: ?0, $options: 'i'}}, " +
            "{'city': {$regex: ?0, $options: 'i'}}, " +
            "{'address': {$regex: ?0, $options: 'i'}}]}")
    List<Pitch> searchByQuery(String query);
}