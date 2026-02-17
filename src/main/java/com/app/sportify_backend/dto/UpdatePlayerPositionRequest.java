package com.app.sportify_backend.dto;

import com.app.sportify_backend.models.Position;
import lombok.Data;

import java.util.List;

@Data
public class UpdatePlayerPositionRequest {
    private List<PlayerPositionUpdate> positions;

    @Data
    public static class PlayerPositionUpdate {
        private String userId;
        private Position position;
        private Double xPosition;
        private Double yPosition;
    }
}
