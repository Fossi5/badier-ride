package com.badier.badier_ride.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.badier.badier_ride.enumeration.RouteStatus;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "routes")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    @ManyToOne
    @JoinColumn(name = "dispatcher_id")
    private User dispatcher;

    // Relation ManyToMany remplacée par OneToMany vers la table d'association
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteDeliveryPoint> routeDeliveryPoints = new ArrayList<>();

    // Maintien de la relation d'origine pour compatibilité avec le code existant
    @ManyToMany
    @JoinTable(
        name = "route_delivery_points",
        joinColumns = @JoinColumn(name = "route_id"),
        inverseJoinColumns = @JoinColumn(name = "delivery_point_id")
    )
    private List<DeliveryPoint> deliveryPoints;

    @Enumerated(EnumType.STRING)
    private RouteStatus status;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    private String notes;
    
    // Méthode utilitaire pour ajouter un point de livraison avec un ordre spécifique
    public void addDeliveryPoint(DeliveryPoint deliveryPoint, Integer order, Boolean isStart, Boolean isEnd) {
        RouteDeliveryPoint rdp = new RouteDeliveryPoint();
        rdp.setRoute(this);
        rdp.setDeliveryPoint(deliveryPoint);
        rdp.setSequenceOrder(order);
        rdp.setIsStartPoint(isStart);
        rdp.setIsEndPoint(isEnd);
        
        this.routeDeliveryPoints.add(rdp);
        
        // Maintenir la liste deliveryPoints synchronisée
        if (this.deliveryPoints == null) {
            this.deliveryPoints = new ArrayList<>();
        }
        if (!this.deliveryPoints.contains(deliveryPoint)) {
            this.deliveryPoints.add(deliveryPoint);
        }
    }
    
    // Méthode pour supprimer un point de livraison
    public void removeDeliveryPoint(DeliveryPoint deliveryPoint) {
        this.routeDeliveryPoints.removeIf(rdp -> rdp.getDeliveryPoint().equals(deliveryPoint));
        this.deliveryPoints.remove(deliveryPoint);
    }
}