package com.badier.badier_ride.dto;

import java.time.LocalDateTime;

import com.badier.badier_ride.enumeration.DeliveryStatus;
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
    private String deliveryTime;   // heure planifiée (HH:mm)
    private String deliveryDate;   // date planifiée (yyyy-MM-dd)
    private DeliveryStatus deliveryStatus;
    private LocalDateTime plannedTime;
    private LocalDateTime actualTime;
    private Integer sequenceOrder;
    private Boolean isStartPoint;
    private Boolean isEndPoint;
    private String proofImagePath;
    private Boolean proofValidated;
    private Boolean hasConfirmationCode;
}
