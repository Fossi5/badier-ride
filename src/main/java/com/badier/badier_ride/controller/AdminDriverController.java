package com.badier.badier_ride.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.badier.badier_ride.dto.DriverRequest;
import com.badier.badier_ride.dto.DriverResponse;
import com.badier.badier_ride.service.AdminDriverService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/drivers")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDriverController {

    private final AdminDriverService adminDriverService;

    @PostMapping
    public ResponseEntity<DriverResponse> createDriver(@RequestBody DriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminDriverService.createDriver(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getDriver(@PathVariable Long id) {
        return ResponseEntity.ok(adminDriverService.getDriverById(id));
    }

    @GetMapping
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {
        return ResponseEntity.ok(adminDriverService.getAllDrivers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DriverResponse> updateDriver(@PathVariable Long id, @RequestBody DriverRequest request) {
        return ResponseEntity.ok(adminDriverService.updateDriver(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        adminDriverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers() {
        return ResponseEntity.ok(adminDriverService.getAvailableDrivers());
    }
}