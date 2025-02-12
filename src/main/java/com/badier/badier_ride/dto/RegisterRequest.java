package com.badier.badier_ride.dto;
import com.badier.badier_ride.enumeration.UserRole;

import lombok.Data;
@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private UserRole role;
}