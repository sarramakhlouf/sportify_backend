package com.app.sportify_backend.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;


@Data
public class CreateReservationRequest {
    private String senderTeamId;
    private String adverseTeamId;
    private String pitchId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate day;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime hour;

    private Integer duration;
}
