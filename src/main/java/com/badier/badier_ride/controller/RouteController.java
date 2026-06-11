package com.badier.badier_ride.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<List<RouteResponse>> getAllRoutes(Authentication auth) {
        boolean isDispatcher = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DISPATCHER"));
        if (isDispatcher) {
            return ResponseEntity.ok(routeService.getRoutesByDispatcherUsername(auth.getName()));
        }
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<Page<RouteResponse>> getAllRoutesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(routeService.getAllRoutesPaged(page, size));
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
    public ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody RouteRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Authenticated user: {}", auth.getName());
        log.debug("Roles: {}", auth.getAuthorities());
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.createRoute(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<RouteResponse> updateRoute(@PathVariable Long id, @Valid @RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeService.updateRoute(id, request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RouteResponse> updateRouteStatus(@PathVariable Long id, @RequestParam String status) {
        RouteResponse route = routeService.getRouteById(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DRIVER"))) {
            if (!route.getDriver().getUsername().equals(auth.getName())) {
                throw new AccessDeniedException("Vous n'avez pas accès à cette tournée");
            }
        }

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

        DeliveryPointResponse deliveryPoint = deliveryPointService.createDeliveryPointFromAddress(
                addressId,
                deliveryPointRequest != null ? deliveryPointRequest : new DeliveryPointRequest());

        return ResponseEntity.ok(routeService.addDeliveryPointToRoute(routeId, deliveryPoint.getId()));
    }

    @PutMapping("/{routeId}/delivery-points/order")
    public ResponseEntity<RouteResponse> updateDeliveryPointsOrder(
            @PathVariable Long routeId,
            @RequestBody RoutePointsOrderRequest request) {

        RouteResponse updatedRoute = routeService.updateRoutePointsOrder(routeId, request.getOrderedPoints());
        return ResponseEntity.ok(updatedRoute);
    }

    @PutMapping("/{routeId}/delivery-points/{deliveryPointId}/status")
    public ResponseEntity<DeliveryPointResponse> updateDeliveryPointStatusInRoute(
            @PathVariable Long routeId,
            @PathVariable Long deliveryPointId,
            @RequestParam String status,
            Authentication authentication) {

        try {
            DeliveryStatus deliveryStatus = DeliveryStatus.valueOf(status);
            return ResponseEntity.ok(deliveryPointService.updateStatusInRoute(routeId, deliveryPointId, deliveryStatus));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Statut invalide: " + status + ". Statuts valides: PENDING, IN_PROGRESS, COMPLETED, FAILED");
        }
    }
}