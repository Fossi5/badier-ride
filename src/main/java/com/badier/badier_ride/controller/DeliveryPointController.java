package com.badier.badier_ride.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.badier.badier_ride.dto.DeliveryPointRequest;
import com.badier.badier_ride.dto.DeliveryPointResponse;
import com.badier.badier_ride.enumeration.DeliveryStatus;
import com.badier.badier_ride.service.DeliveryPointService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/delivery-points")
@RequiredArgsConstructor
public class DeliveryPointController {
    
    private final DeliveryPointService deliveryPointService;
    
    @GetMapping
    public ResponseEntity<List<DeliveryPointResponse>> getAllDeliveryPoints() {
        return ResponseEntity.ok(deliveryPointService.getAllDeliveryPoints());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DeliveryPointResponse> getDeliveryPoint(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryPointService.getDeliveryPointById(id));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeliveryPointResponse>> getDeliveryPointsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(deliveryPointService.getDeliveryPointsByStatus(DeliveryStatus.valueOf(status)));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<DeliveryPointResponse> createDeliveryPoint(@RequestBody DeliveryPointRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryPointService.createDeliveryPoint(request));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<DeliveryPointResponse> updateDeliveryPoint(
            @PathVariable Long id, 
            @RequestBody DeliveryPointRequest request) {
        return ResponseEntity.ok(deliveryPointService.updateDeliveryPoint(id, request));
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<DeliveryPointResponse> updateDeliveryPointStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        return ResponseEntity.ok(deliveryPointService.updateStatus(id, DeliveryStatus.valueOf(status)));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<Void> deleteDeliveryPoint(@PathVariable Long id) {
        deliveryPointService.deleteDeliveryPoint(id);
        return ResponseEntity.noContent().build();
    }
}