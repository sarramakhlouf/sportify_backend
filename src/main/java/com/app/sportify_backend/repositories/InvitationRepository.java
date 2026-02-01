package com.app.sportify_backend.repositories;

import com.app.sportify_backend.models.Invitation;
import com.app.sportify_backend.models.InvitationStatus;
import com.app.sportify_backend.models.InvitationType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends MongoRepository<Invitation, String> {
    List<Invitation> findBySenderId(String senderId);

    Optional<Invitation> findByTeamIdAndReceiverIdAndStatus(
            String teamId,
            String receiverId,
            InvitationStatus status
    );

    Optional<Invitation> findBySenderTeamIdAndReceiverTeamIdAndStatus(
            String senderTeamId,
            String receiverTeamId,
            InvitationStatus status
    );

    List<Invitation> findByTypeAndReceiverIdAndStatus(
            InvitationType type,
            String receiverId,
            InvitationStatus status
    );

    List<Invitation> findByTypeAndReceiverTeamIdAndStatus(
            InvitationType type,
            String receiverTeamId,
            InvitationStatus status
    );

    List<Invitation> findByTypeAndReceiverTeamId(
            InvitationType type,
            String receiverTeamId
    );

    long countByTypeAndReceiverIdAndStatus(
            InvitationType type,
            String receiverId,
            InvitationStatus status
    );

    long countByTypeAndReceiverTeamIdAndStatus(
            InvitationType type,
            String receiverTeamId,
            InvitationStatus status
    );

    List<Invitation> findByTypeAndSenderIdOrReceiverId(
            InvitationType type,
            String senderId,
            String receiverId
    );

    List<Invitation> findByTypeAndSenderIdAndDeletedBySenderFalse(
            InvitationType type,
            String senderId
    );

    List<Invitation> findByTypeAndReceiverIdAndDeletedByReceiverFalse(
            InvitationType type,
            String receiverId
    );

    List<Invitation> findByTypeAndReceiverTeamIdAndDeletedByReceiverFalse(
            InvitationType type,
            String receiverTeamId
    );

    List<Invitation> findByTypeAndSenderTeamIdAndDeletedBySenderFalse(
            InvitationType type,
            String senderTeamId
    );

    /*long countByReceiverIdAndStatus(String receiverId, InvitationStatus status);
    List<Invitation> findByReceiverIdAndStatus(String receiverId, InvitationStatus status);
    List<Invitation> findBySenderIdAndStatus(String senderId, InvitationStatus status);*/
}

