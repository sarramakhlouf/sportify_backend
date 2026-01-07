package com.app.sportify_backend.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamPlayerResponse {

    private String userId;
    private String firstname;
    private String lastname;
    private String email;
    private String profileImage;
    private String playerCode;

    private String role; // OWNER / MEMBER
}
