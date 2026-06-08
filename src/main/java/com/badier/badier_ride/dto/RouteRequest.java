package com.badier.badier_ride.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {
    @NotBlank(message = "Le nom de la tournée est obligatoire")
    private String name;

    @NotNull(message = "L'identifiant du chauffeur est obligatoire")
    private Long driverId;

    @NotNull(message = "L'identifiant du dispatcher est obligatoire")
    private Long dispatcherId;

    private List<Long> deliveryPointIds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String notes;

    @NotBlank(message = "Le statut de la tournée est obligatoire")
    private String status;
}