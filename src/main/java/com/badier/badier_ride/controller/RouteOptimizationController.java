package com.badier.badier_ride.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.badier.badier_ride.entity.Route;
import com.badier.badier_ride.service.RouteOptimizationService;
import com.badier.badier_ride.service.RouteService;

import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur pour les opérations d'optimisation des tournées
 * Permet d'optimiser l'ordre des points de livraison et de calculer les distances
 */
@RestController
@RequestMapping("/api/routes")
@Slf4j
public class RouteOptimizationController {
    
    @Autowired
    private RouteOptimizationService routeOptimizationService;
    
    @Autowired
    private RouteService routeService;
    
    /**
     * Endpoint pour optimiser l'ordre des points de livraison dans une tournée
     * avec prise en compte des fenêtres de temps et des priorités
     */
    @PostMapping("/{id}/optimize")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<?> optimizeRoute(@PathVariable Long id) {
        log.info("Demande d'optimisation pour la tournée: {}", id);
        
        Route optimizedRoute = routeOptimizationService.optimizeRoute(id);
        
        // Au lieu de dépendre de mapToResponse, renvoyons directement l'entité optimisée
        // Le corps de la réponse sera automatiquement converti par Spring
        return ResponseEntity.ok(optimizedRoute);
    }
    
    /**
     * Endpoint pour calculer la distance totale d'une tournée
     * en utilisant Google Maps quand disponible
     */
    @GetMapping("/{id}/distance")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<Double> calculateRouteDistance(@PathVariable Long id) {
        log.info("Demande de calcul de distance pour la tournée: {}", id);
        
        double totalDistance = routeOptimizationService.calculateTotalRouteDistance(id);
        
        return ResponseEntity.ok(totalDistance);
    }
}