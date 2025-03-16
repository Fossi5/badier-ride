// DriverController.java
package com.badier.badier_ride.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.badier.badier_ride.dto.DriverProfileRequest;
import com.badier.badier_ride.dto.DriverResponse;
import com.badier.badier_ride.service.DriverService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(driverService.getDriverProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverResponse> updateProfile(
            Authentication authentication,
            @RequestBody DriverProfileRequest request) {
        return ResponseEntity.ok(driverService.updateDriverProfile(authentication.getName(), request));
    }

    @PutMapping("/availability")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverResponse> updateAvailability(
            Authentication authentication,
            @RequestParam Boolean isAvailable) {
        return ResponseEntity.ok(driverService.updateDriverAvailability(authentication.getName(), isAvailable));
    }
}
