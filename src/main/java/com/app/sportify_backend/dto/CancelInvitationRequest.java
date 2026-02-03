package com.app.sportify_backend.dto;

import com.app.sportify_backend.models.CancelReason;
import lombok.Data;

@Data
public class CancelInvitationRequest {
    private CancelReason reason;
    private String message;
}

