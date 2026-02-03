package com.app.sportify_backend.dto;

import lombok.Data;

@Data
public class PitchDTO {
    private String id;
    private String name;
    private String address;
    private String city;
    private Double price;
    private String size;
    private String rating;
    private String surfaceType;
    private String imageUrl;
    private boolean isActive;
}