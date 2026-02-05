package com.app.sportify_backend.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "pitches")
@Getter
@Setter
public class Pitch {

    @Id
    private String id;

    private String name;
    private String address;
    private String city;

    private Double price;

    private String size;
    private String rating;
    private String surfaceType;
    private String imageUrl;
    //private List<String> amenities;

    private String createdBy;
    private boolean createdViaValidation = false;
    private boolean createdViaBackoffice = false;
    private boolean isActive = true;

    private LocalDateTime createdAt;

}