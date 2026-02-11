package com.badier.badier_ride.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import com.badier.badier_ride.dto.DeliveryPointRequest;
import com.badier.badier_ride.dto.DeliveryPointResponse;
import com.badier.badier_ride.dto.RoutePointsOrderRequest;
import com.badier.badier_ride.dto.RouteRequest;
import com.badier.badier_ride.dto.RouteResponse;
import com.badier.badier_ride.enumeration.DeliveryStatus;
import com.badier.badier_ride.service.DeliveryPointService;
import com.badier.badier_ride.service.RouteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private static final Logger log = LoggerFactory.getLogger(RouteController.class);

    private final RouteService routeService;
    private final DeliveryPointService deliveryPointService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<RouteResponse>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @GetMapping("/driver")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<RouteResponse>> getMyRoutes(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(routeService.getRoutesByDriverUsername(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> getRoute(@PathVariable Long id) {
        RouteResponse route = routeService.getRouteById(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Si c'est un driver, vérifier que la tournée lui est assignée
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DRIVER"))) {
            if (!route.getDriver().getUsername().equals(auth.getName())) {
                throw new AccessDeniedException("Vous n'avez pas accès à cette tournée");
            }
        }

        return ResponseEntity.ok(route);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<RouteResponse>> getRoutesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(routeService.getRoutesByStatus(status));
    }

    @GetMapping("/dispatcher/{dispatcherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<RouteResponse>> getRoutesByDispatcher(@PathVariable Long dispatcherId) {
        return ResponseEntity.ok(routeService.getRoutesByDispatcher(dispatcherId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<RouteResponse> createRoute(@RequestBody RouteRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authenticated user: " + auth.getName());
        System.out.println("Roles: " + auth.getAuthorities());
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.createRoute(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<RouteResponse> updateRoute(@PathVariable Long id, @RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeService.updateRoute(id, request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RouteResponse> updateRouteStatus(@PathVariable Long id, @RequestParam String status) {
        RouteResponse route = routeService.getRouteById(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Si c'est un driver, vérifier que la tournée lui est assignée
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DRIVER"))) {
            if (!route.getDriver().getUsername().equals(auth.getName())) {
                throw new AccessDeniedException("Vous n'avez pas accès à cette tournée");
            }
        }

        // Le GlobalExceptionHandler gère automatiquement les IllegalStateException
        return ResponseEntity.ok(routeService.updateRouteStatus(id, status));
    }

    @PostMapping("/{routeId}/delivery-points/{deliveryPointId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<RouteResponse> addDeliveryPointToRoute(
            @PathVariable Long routeId,
            @PathVariable Long deliveryPointId) {
        return ResponseEntity.ok(routeService.addDeliveryPointToRoute(routeId, deliveryPointId));
    }

    @DeleteMapping("/{routeId}/delivery-points/{deliveryPointId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<RouteResponse> removeDeliveryPointFromRoute(
            @PathVariable Long routeId,
            @PathVariable Long deliveryPointId) {
        return ResponseEntity.ok(routeService.removeDeliveryPointFromRoute(routeId, deliveryPointId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{routeId}/addresses/{addressId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<RouteResponse> addAddressToRoute(
            @PathVariable Long routeId,
            @PathVariable Long addressId,
            @RequestBody(required = false) DeliveryPointRequest deliveryPointRequest) {

        // Créer un point de livraison avec cette adresse si nécessaire
        DeliveryPointResponse deliveryPoint = deliveryPointService.createDeliveryPointFromAddress(
                addressId,
                deliveryPointRequest != null ? deliveryPointRequest : new DeliveryPointRequest());

        // Ajouter ce point de livraison à la route
        return ResponseEntity.ok(routeService.addDeliveryPointToRoute(routeId, deliveryPoint.getId()));
    }

    @PutMapping("/{routeId}/delivery-points/order")
    public ResponseEntity<RouteResponse> updateDeliveryPointsOrder(
            @PathVariable Long routeId,
            @RequestBody RoutePointsOrderRequest request) {

        RouteResponse updatedRoute = routeService.updateRoutePointsOrder(routeId, request.getOrderedPoints());
        return ResponseEntity.ok(updatedRoute);
    }

    /**
     * Met à jour le statut d'un point de livraison dans le contexte d'une tournée
     * spécifique.
     * Nouveau endpoint recommandé pour éviter les conflits de statut entre
     * tournées.
     */
    @PutMapping("/{routeId}/delivery-points/{deliveryPointId}/status")
    public ResponseEntity<DeliveryPointResponse> updateDeliveryPointStatusInRoute(
            @PathVariable Long routeId,
            @PathVariable Long deliveryPointId,
            @RequestParam String status,
            Authentication authentication) {

        log.info("Mise à jour du statut du point {} dans la tournée {} vers {} par {}",
                deliveryPointId, routeId, status, authentication.getName());

        try {
            DeliveryStatus deliveryStatus = DeliveryStatus.valueOf(status);
            DeliveryPointResponse response = deliveryPointService.updateStatusInRoute(routeId, deliveryPointId,
                    deliveryStatus);
            log.info("Statut mis à jour avec succès pour le point {} dans la tournée {}", deliveryPointId, routeId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Statut invalide: {}. Statuts valides: PENDING, IN_PROGRESS, COMPLETED, FAILED", status);
            throw new RuntimeException(
                    "Statut invalide: " + status + ". Statuts valides: PENDING, IN_PROGRESS, COMPLETED, FAILED");
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du statut pour le point {} dans la tournée {}: {}",
                    deliveryPointId, routeId, e.getMessage(), e);
            throw e;
        }
    }
}