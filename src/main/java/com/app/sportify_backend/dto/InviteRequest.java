package com.app.sportify_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InviteRequest {
    private String teamId;
    private String senderId;
    private String playerCode;
}
