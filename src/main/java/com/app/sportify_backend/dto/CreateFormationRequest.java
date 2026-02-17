package com.app.sportify_backend.dto;

import com.app.sportify_backend.models.FormationType;
import com.app.sportify_backend.models.Position;
import lombok.Data;

@Data
public class CreateFormationRequest {
    private String teamId;
    private FormationType formationType;
    private String ownerId;
    private Position ownerPreferredPosition;
}