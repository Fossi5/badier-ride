package com.badier.badier_ride.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.badier.badier_ride.entity.RouteDeliveryPoint;

@Repository
public interface RouteDeliveryPointRepository extends JpaRepository<RouteDeliveryPoint, Long> {

    List<RouteDeliveryPoint> findByRouteIdOrderBySequenceOrderAsc(Long routeId);

    void deleteByRouteId(Long routeId);

    // Récupérer un point de livraison spécifique dans une tournée
    Optional<RouteDeliveryPoint> findByRouteIdAndDeliveryPointId(Long routeId, Long deliveryPointId);

    void deleteByRouteIdAndDeliveryPointId(Long routeId, Long deliveryPointId);
}
