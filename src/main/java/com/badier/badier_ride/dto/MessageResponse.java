package com.badier.badier_ride.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private String content;
    private String senderUsername;
    private String senderRole;
    private Long routeId;
    private LocalDateTime sentAt;
    private Boolean isRead;
}
