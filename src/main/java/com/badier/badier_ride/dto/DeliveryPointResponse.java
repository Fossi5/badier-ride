package com.badier.badier_ride.dto;

import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPointResponse {
    private Long id;
    private AddressResponse address;
    private String clientName;
    private String clientPhoneNumber;
    private String clientEmail;
    private String clientNote;
    private String deliveryNote;
    private String deliveryTime;
    private String deliveryDate;
    private String deliveryStatus;
    private LocalDateTime plannedTime;
    private LocalDateTime actualTime;
    
    // Getters et Setters
}
