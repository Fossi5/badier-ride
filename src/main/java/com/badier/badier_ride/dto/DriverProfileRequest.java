package com.badier.badier_ride.dto;

import lombok.Data;

@Data
public class DriverProfileRequest {
    private String phoneNumber;
    private Double latitude;
    private Double longitude;
}
