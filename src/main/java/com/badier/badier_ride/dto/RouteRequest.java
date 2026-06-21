package com.badier.badier_ride.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.badier.badier_ride.enumeration.RecurrenceType;
import com.badier.badier_ride.validation.EndAfterStart;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@EndAfterStart
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {
    @NotBlank(message = "Le nom de la tournée est obligatoire")
    private String name;

    @NotNull(message = "L'identifiant du chauffeur est obligatoire")
    private Long driverId;

    private Long dispatcherId;

    private List<Long> deliveryPointIds;
    @FutureOrPresent(message = "La date de début ne peut pas être dans le passé")
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String notes;

    @NotBlank(message = "Le statut de la tournée est obligatoire")
    private String status;

    @Builder.Default
    private Boolean recurring = false;
    private RecurrenceType recurrenceType;
    private LocalDate recurrenceEndDate;
}