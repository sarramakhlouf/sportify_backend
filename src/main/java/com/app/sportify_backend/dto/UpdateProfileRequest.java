package com.app.sportify_backend.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String password;
    private String currentPassword;
}
