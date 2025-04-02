package com.badier.badier_ride.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.badier.badier_ride.dto.DispatcherRequest;
import com.badier.badier_ride.dto.DispatcherResponse;
import com.badier.badier_ride.service.AdminDispatcherService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/dispatchers")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDispatcherController {

    private final AdminDispatcherService adminDispatcherService;

    @PostMapping
    public ResponseEntity<DispatcherResponse> createDispatcher(@RequestBody DispatcherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminDispatcherService.createDispatcher(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DispatcherResponse> getDispatcher(@PathVariable Long id) {
        return ResponseEntity.ok(adminDispatcherService.getDispatcherById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<DispatcherResponse>> getAllDispatchers() {
        return ResponseEntity.ok(adminDispatcherService.getAllDispatchers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DispatcherResponse> updateDispatcher(@PathVariable Long id,
            @RequestBody DispatcherRequest request) {
        return ResponseEntity.ok(adminDispatcherService.updateDispatcher(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDispatcher(@PathVariable Long id) {
        adminDispatcherService.deleteDispatcher(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<List<DispatcherResponse>> getDispatchersByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(adminDispatcherService.getDispatchersByDepartment(department));
    }
}