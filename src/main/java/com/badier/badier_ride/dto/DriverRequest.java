package com.badier.badier_ride.dto;

import lombok.Data;

@Data
public class DriverRequest {
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private String vehicleType;
    private Boolean isAvailable;
    private Double latitude;
    private Double longitude;
}
