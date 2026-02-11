package com.badier.badier_ride.controller;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(DeliveryPointController.class);

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
        // Logs de débogage
        logger.info("Requête reçue pour createDeliveryPoint: {}", request);
        try {
            DeliveryPointResponse response = deliveryPointService.createDeliveryPoint(request);
            logger.info("DeliveryPoint créé avec succès: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du DeliveryPoint: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<DeliveryPointResponse> updateDeliveryPoint(
            @PathVariable Long id,
            @RequestBody DeliveryPointRequest request) {
        return ResponseEntity.ok(deliveryPointService.updateDeliveryPoint(id, request));
    }

    /**
     * @deprecated Utiliser updateDeliveryPointStatusInRoute() à la place
     *             Cet endpoint met à jour le statut global du point (pas
     *             recommandé)
     */
    @Deprecated
    @PutMapping("/{id}/status")
    public ResponseEntity<DeliveryPointResponse> updateDeliveryPointStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        logger.warn(
                "Utilisation de l'ancien endpoint updateDeliveryPointStatus (deprecated). Utiliser /routes/{routeId}/delivery-points/{id}/status à la place");
        logger.info("Mise à jour du statut du point de livraison {} vers {}", id, status);
        try {
            DeliveryStatus deliveryStatus = DeliveryStatus.valueOf(status);
            DeliveryPointResponse response = deliveryPointService.updateStatus(id, deliveryStatus);
            logger.info("Statut mis à jour avec succès pour le point {}", id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Statut invalide: {}. Statuts valides: PENDING, IN_PROGRESS, COMPLETED, FAILED", status);
            throw new RuntimeException(
                    "Statut invalide: " + status + ". Statuts valides: PENDING, IN_PROGRESS, COMPLETED, FAILED");
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du statut pour le point {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<Void> deleteDeliveryPoint(@PathVariable Long id) {
        deliveryPointService.deleteDeliveryPoint(id);
        return ResponseEntity.noContent().build();
    }
}