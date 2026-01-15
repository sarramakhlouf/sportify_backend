package com.app.sportify_backend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "teams")
public class Team {

    @Id
    private String id;

    private String name;
    private String city;
    private String color;
    private String logoUrl;
    private Boolean isActivated = false;

    private String ownerId;
    private String teamCode;

    private List<TeamMember> members = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TeamMember {
        private String userId;
        private MemberRole role;
    }
}
