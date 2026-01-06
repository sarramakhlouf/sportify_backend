package com.app.sportify_backend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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


}
