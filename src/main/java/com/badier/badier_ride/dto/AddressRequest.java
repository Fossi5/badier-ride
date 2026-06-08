package com.badier.badier_ride.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    private Long id;

    @NotBlank(message = "La rue est obligatoire")
    private String street;

    @NotBlank(message = "La ville est obligatoire")
    private String city;

    @NotBlank(message = "Le code postal est obligatoire")
    private String postalCode;

    private String country;
    private Double latitude;
    private Double longitude;
    private Boolean isVerified;
}
