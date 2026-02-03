package com.app.sportify_backend.repositories;

import com.app.sportify_backend.models.Match;
import com.app.sportify_backend.models.MatchStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends MongoRepository<Match, String> {
    List<Match> findBySenderTeamIdOrReceiverTeamId(String senderTeamId, String receiverTeamId);
    List<Match> findByPitchIdAndMatchDate(String pitchId, LocalDate matchDate);
    List<Match> findByStatus(MatchStatus status);
    List<Match> findByCreatedBy(String createdBy);
}