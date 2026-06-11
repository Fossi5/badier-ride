package com.badier.badier_ride.controller;

import com.badier.badier_ride.dto.AlertRequest;
import com.badier.badier_ride.dto.AlertResponse;
import com.badier.badier_ride.enumeration.AlertStatus;
import com.badier.badier_ride.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<List<AlertResponse>> getAllAlerts(@RequestParam(required = false) String status) {
        if (status != null) {
            return ResponseEntity.ok(alertService.getAlertsByStatus(AlertStatus.valueOf(status)));
        }
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @PostMapping
    public ResponseEntity<AlertResponse> createAlert(@Valid @RequestBody AlertRequest request, Authentication auth) {
        return ResponseEntity.ok(alertService.createAlert(request, auth.getName()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AlertResponse> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(alertService.updateAlertStatus(id, body.get("status")));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<AlertResponse> resolve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(alertService.resolveAlert(id, body.get("resolutionNote")));
    }
}
