package com.badier.badier_ride.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AlertResponse {
    private Long id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private String dispatcherUsername;
    private String driverUsername;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String resolutionNote;
}
