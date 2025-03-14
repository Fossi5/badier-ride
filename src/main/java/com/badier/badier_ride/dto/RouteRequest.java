package com.badier.badier_ride.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {
    private String name;
    private Long driverId;
    private Long dispatcherId;
    private List<Long> deliveryPointIds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String notes;
    private String status;
}