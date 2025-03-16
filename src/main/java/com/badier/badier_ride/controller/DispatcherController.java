package com.badier.badier_ride.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.badier.badier_ride.dto.DispatcherProfileRequest;
import com.badier.badier_ride.dto.DispatcherResponse;
import com.badier.badier_ride.service.DispatcherService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dispatcher")
@RequiredArgsConstructor
public class DispatcherController {

    private final DispatcherService dispatcherService;

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<DispatcherResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(dispatcherService.getDispatcherProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<DispatcherResponse> updateProfile(
            Authentication authentication, 
            @RequestBody DispatcherProfileRequest request) {
        return ResponseEntity.ok(dispatcherService.updateDispatcherProfile(authentication.getName(), request));
    }
}

