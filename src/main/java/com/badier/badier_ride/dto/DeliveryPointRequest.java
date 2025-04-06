package com.badier.badier_ride.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPointRequest {
    private Long addressId;
    private AddressRequest address; 
    private String clientName;
    private String clientPhoneNumber;
    private String clientEmail;
    private String clientNote;
    private String deliveryNote;
    private String deliveryTime;
    private String deliveryDate;
    private String deliveryStatus;
    
    // Getters et Setters
}
