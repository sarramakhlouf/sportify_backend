package com.app.sportify_backend.repositories;

import com.app.sportify_backend.models.Invitation;
import com.app.sportify_backend.models.InvitationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InvitationRepository extends MongoRepository<Invitation, String> {
    List<Invitation> findByReceiverIdAndStatus(String receiverId, InvitationStatus status);
}

