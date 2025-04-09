package com.badier.badier_ride.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.badier.badier_ride.dto.RouteResponse;
import com.badier.badier_ride.entity.Route;
import com.badier.badier_ride.service.RouteOptimizationService;
import com.badier.badier_ride.service.RouteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/routes/optimization")
@RequiredArgsConstructor
public class RouteOptimizationController {

    private final RouteOptimizationService optimizationService;
    private final RouteService routeService;

    /**
     * Optimise une tournée sans contraintes particulières
     */
    @PostMapping("/{routeId}")
    public ResponseEntity<RouteResponse> optimizeRoute(@PathVariable Long routeId) {
        Route optimizedRoute = optimizationService.optimizeRoute(routeId);
        return ResponseEntity.ok(routeService.mapToResponse(optimizedRoute));
    }
    
    /**
     * Optimise une tournée en respectant les points de départ et d'arrivée définis
     */
    @PostMapping("/{routeId}/with-fixed-points")
    public ResponseEntity<RouteResponse> optimizeRouteWithFixedPoints(@PathVariable Long routeId) {
        Route optimizedRoute = optimizationService.optimizeRouteWithFixedPoints(routeId);
        return ResponseEntity.ok(routeService.mapToResponse(optimizedRoute));
    }
    
    /**
     * Calcule la distance totale d'une tournée
     */
    @GetMapping("/{routeId}/distance")
    public ResponseEntity<Double> calculateRouteDistance(@PathVariable Long routeId) {
        double distance = optimizationService.calculateTotalRouteDistance(routeId);
        return ResponseEntity.ok(distance);
    }
}