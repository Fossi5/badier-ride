package com.badier.badier_ride.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlertRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotNull
    private String priority;
    private Long routeId;
    private Long driverId;
}
