package com.badier.badier_ride.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.badier.badier_ride.entity.DeliveryPoint;
import com.badier.badier_ride.enumeration.DeliveryStatus;
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

    // Relation OneToMany vers la table d'association enrichie
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteDeliveryPoint> routeDeliveryPoints = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private RouteStatus status;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    private String notes;

    /**
     * Retourne une vue immuable des points de livraison assignés à cette tournée.
     * Cette méthode remplace l'ancien accès direct via la relation ManyToMany.
     */
    public List<DeliveryPoint> getDeliveryPointsSnapshot() {
        return routeDeliveryPoints.stream()
                .map(RouteDeliveryPoint::getDeliveryPoint)
                .collect(Collectors.toUnmodifiableList());
    }

    // Méthode utilitaire pour ajouter un point de livraison avec un ordre
    // spécifique
    public void addDeliveryPoint(DeliveryPoint deliveryPoint, Integer order, Boolean isStart, Boolean isEnd) {
        RouteDeliveryPoint rdp = new RouteDeliveryPoint();
        rdp.setRoute(this);
        rdp.setDeliveryPoint(deliveryPoint);
        rdp.setSequenceOrder(order);
        rdp.setIsStartPoint(isStart);
        rdp.setIsEndPoint(isEnd);
        rdp.setStatus(DeliveryStatus.PENDING);
        rdp.setPlannedTime(deliveryPoint.getPlannedTime());

        this.routeDeliveryPoints.add(rdp);
    }

    // Méthode pour supprimer un point de livraison
    public void removeDeliveryPoint(DeliveryPoint deliveryPoint) {
        this.routeDeliveryPoints.removeIf(rdp -> rdp.getDeliveryPoint().equals(deliveryPoint));
    }
}