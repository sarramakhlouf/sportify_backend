package com.app.sportify_backend.repositories;

import com.app.sportify_backend.models.Reservation;
import com.app.sportify_backend.models.ReservationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends MongoRepository<Reservation, String> {

    Optional<Reservation> findByPitchIdAndDayAndHourAndStatus(
            String pitchId,
            LocalDate day,
            LocalTime hour,
            ReservationStatus status
    );
    //List<Reservation> findByPitchId(String pitchId);
    //List<Reservation> findByPitchIdAndStatus(String pitchId, ReservationStatus status);
    List<Reservation> findBySenderTeamIdAndCancelledBySenderFalse(String senderTeamId);
    //List<Reservation> findByReceiverIdAndCancelledByReceiverFalse(String receiverId);
    List<Reservation> findByReceiverIdAndStatus(String receiverId, ReservationStatus status);
    List<Reservation> findByAdverseTeamId(String adverseTeamId);
    List<Reservation> findByPitchIdAndDay(String pitchId, LocalDate day);
    List<Reservation> findByPitchIdAndStatusOrderByDayAscHourAsc(
            String pitchId,
            ReservationStatus status
    );
    List<Reservation> findByStatusAndSenderIdOrReceiverId(
            ReservationStatus status,
            String senderId,
            String receiverId
    );

    List<Reservation> findBySenderTeamIdAndStatus(String senderTeamId, ReservationStatus status);

    @Query("{ 'status': ?0, $or: [ { 'senderTeamId': ?1 }, { 'adverseTeamId': ?1 } ] }")
    List<Reservation> findAllCompletedByTeam(ReservationStatus status, String teamId);

}