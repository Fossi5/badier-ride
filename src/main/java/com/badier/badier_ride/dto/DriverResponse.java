package com.badier.badier_ride.dto;

import lombok.Data;

@Data
public class DriverResponse {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String vehicleType;
    private Boolean isAvailable;
    private Double latitude;
    private Double longitude;
    private String lastLocationUpdate;
}
