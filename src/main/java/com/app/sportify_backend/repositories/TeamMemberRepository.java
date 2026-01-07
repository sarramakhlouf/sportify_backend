package com.app.sportify_backend.repositories;

import com.app.sportify_backend.models.TeamMember;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TeamMemberRepository extends MongoRepository<TeamMember, String> {
    List<TeamMember> findByUserId(String userId);
    List<TeamMember> findByTeamId(String teamId);
    boolean existsByTeamIdAndUserId(String teamId, String userId);
}

