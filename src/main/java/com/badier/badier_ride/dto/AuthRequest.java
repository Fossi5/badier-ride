package com.badier.badier_ride.dto;


import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}