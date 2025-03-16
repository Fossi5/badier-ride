package com.badier.badier_ride.dto;

import lombok.Data;

@Data
public class DispatcherRequest {
    private String username;
    private String password;
    private String email;
    private String department;
}
