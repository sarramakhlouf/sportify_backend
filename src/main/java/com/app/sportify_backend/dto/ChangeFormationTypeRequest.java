package com.app.sportify_backend.dto;

import com.app.sportify_backend.models.FormationType;
import lombok.Data;

@Data
public class ChangeFormationTypeRequest {
    private FormationType formationType;
}