package com.badier.badier_ride.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.badier.badier_ride.dto.RouteRequest;
import com.badier.badier_ride.dto.RouteResponse;
import com.badier.badier_ride.service.RouteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping
    public ResponseEntity<RouteResponse> createRoute(@RequestBody RouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.createRoute(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> getRoute(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @GetMapping
    public ResponseEntity<List<RouteResponse>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<RouteResponse>> getRoutesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(routeService.getRoutesByStatus(status));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<RouteResponse>> getRoutesByDriver(@PathVariable Long driverId) {
        return ResponseEntity.ok(routeService.getRoutesByDriver(driverId));
    }

    @GetMapping("/dispatcher/{dispatcherId}")
    public ResponseEntity<List<RouteResponse>> getRoutesByDispatcher(@PathVariable Long dispatcherId) {
        return ResponseEntity.ok(routeService.getRoutesByDispatcher(dispatcherId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteResponse> updateRoute(@PathVariable Long id, @RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeService.updateRoute(id, request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RouteResponse> updateRouteStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(routeService.updateRouteStatus(id, status));
    }

    @PostMapping("/{routeId}/delivery-points/{deliveryPointId}")
    public ResponseEntity<RouteResponse> addDeliveryPointToRoute(
            @PathVariable Long routeId, 
            @PathVariable Long deliveryPointId) {
        return ResponseEntity.ok(routeService.addDeliveryPointToRoute(routeId, deliveryPointId));
    }

    @DeleteMapping("/{routeId}/delivery-points/{deliveryPointId}")
    public ResponseEntity<RouteResponse> removeDeliveryPointFromRoute(
            @PathVariable Long routeId, 
            @PathVariable Long deliveryPointId) {
        return ResponseEntity.ok(routeService.removeDeliveryPointFromRoute(routeId, deliveryPointId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }
}