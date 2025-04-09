package com.badier.badier_ride.entity;

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
}