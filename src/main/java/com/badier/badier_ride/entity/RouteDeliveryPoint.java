package com.badier.badier_ride.entity;

import java.time.LocalDateTime;

import com.badier.badier_ride.enumeration.DeliveryStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "route_delivery_points")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteDeliveryPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @ManyToOne
    @JoinColumn(name = "delivery_point_id")
    private DeliveryPoint deliveryPoint;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "is_start_point")
    private Boolean isStartPoint;

    @Column(name = "is_end_point")
    private Boolean isEndPoint;

    // Statut spécifique à cette tournée (pas global)
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PENDING;

    // Heure planifiée pour cette tournée
    @Column(name = "planned_time")
    private LocalDateTime plannedTime;

    // Heure réelle de livraison pour cette tournée
    @Column(name = "actual_time")
    private LocalDateTime actualTime;
}