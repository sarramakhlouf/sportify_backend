package com.app.sportify_backend.models;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
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
    private String logoUrl;
    private Boolean isActivated = false;

    private String ownerId;
    private String teamCode;

    private List<TeamMember> members = new ArrayList<>();

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TeamMember {
        private String userId;
        private String userFirstName;
        private String userLastName;
        private MemberRole role;
    }
}
