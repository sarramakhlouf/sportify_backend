package com.app.sportify_backend.dto;

import lombok.Data;

@Data
public class UpdateTeamRequest {
    private String name;
    private String city;
    private String logoUrl;
}
