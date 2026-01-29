package com.app.sportify_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitePlayerRequest {
    private String teamId;
    private String senderId;
    private String playerCode;
}