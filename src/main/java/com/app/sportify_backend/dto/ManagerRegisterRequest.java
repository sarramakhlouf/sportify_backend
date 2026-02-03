package com.app.sportify_backend.dto;

import com.app.sportify_backend.models.Pitch;
import com.app.sportify_backend.models.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ManagerRegisterRequest extends RegisterRequest {

    private Pitch pitch;

    public ManagerRegisterRequest(
            String firstname,
            String lastname,
            String email,
            String phone,
            String password,
            Pitch pitch
    ) {
        super(
                firstname,
                lastname,
                email,
                phone,
                password,
                Role.MANAGER
        );
        this.pitch = pitch;
    }
}