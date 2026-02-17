package com.app.sportify_backend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "formations")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@CompoundIndex(
        name = "reservation_team_unique",
        def = "{'reservationId': 1, 'teamId': 1}",
        unique = true )
public class Formation {

    @Id
    private String id;

    private String teamId;
    private FormationType formationType;

    private List<PlayerPosition> playerPositions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PlayerPosition {
        private String userId;
        private String playerName;
        private String playerImageUrl;
        private Position position;
        private Double xPosition;
        private Double yPosition;
    }
}