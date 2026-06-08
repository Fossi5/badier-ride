package com.badier.badier_ride.dto;

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
public class DeliveryPointRequest {
    private Long addressId;
    private AddressRequest address;

    @NotBlank(message = "Le nom du client est obligatoire")
    private String clientName;

    private String clientPhoneNumber;
    private String clientEmail;
    private String clientNote;
    private String deliveryNote;

    @NotBlank(message = "L'heure de livraison est obligatoire")
    private String deliveryTime;

    private String deliveryDate;

    @NotBlank(message = "Le statut de livraison est obligatoire")
    private String deliveryStatus;
}
