package com.badier.badier_ride.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.badier.badier_ride.entity.Route;
import com.badier.badier_ride.enumeration.RouteStatus;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByStatus(RouteStatus status);
    List<Route> findByDriverId(Long driverId);
    List<Route> findByDispatcherId(Long dispatcherId);
    List<Route> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Route> findByDeliveryPointsId(Long deliveryPointId);
}