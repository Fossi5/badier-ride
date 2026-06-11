package com.badier.badier_ride.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String message;
    private String type;
    private String senderUsername;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
