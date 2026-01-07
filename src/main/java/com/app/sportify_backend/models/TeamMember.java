package com.app.sportify_backend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "team_members")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamMember {

    @Id
    private String id;

    private String teamId;
    private String userId;

    private MemberRole role;
}
