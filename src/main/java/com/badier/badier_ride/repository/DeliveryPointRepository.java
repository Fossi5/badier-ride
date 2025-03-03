package com.badier.badier_ride.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.badier.badier_ride.entity.DeliveryPoint;
import com.badier.badier_ride.enumeration.DeliveryStatus;

@Repository
public interface DeliveryPointRepository extends JpaRepository<DeliveryPoint, Long> {
    // Trouver par statut
    List<DeliveryPoint> findByStatus(DeliveryStatus status);
    
    // Trouver par adresse
    List<DeliveryPoint> findByAddressId(Long addressId);
    
    // Trouver par nom de client
    List<DeliveryPoint> findByClientNameContaining(String clientName);
}