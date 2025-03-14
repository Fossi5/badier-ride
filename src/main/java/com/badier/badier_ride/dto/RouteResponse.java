package com.badier.badier_ride.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.badier.badier_ride.enumeration.RouteStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {
    private Long id;
    private String name;
    private UserSummaryResponse driver;
    private UserSummaryResponse dispatcher;
    private List<DeliveryPointResponse> deliveryPoints;
    private RouteStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String notes;
}