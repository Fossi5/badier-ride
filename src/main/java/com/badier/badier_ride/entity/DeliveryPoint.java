package com.badier.badier_ride.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.badier.badier_ride.enumeration.DeliveryStatus;

@Data
@Entity
@Table(name = "delivery_points")
public class DeliveryPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "client_phone")
    private String clientPhone;

    private String notes;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @Column(name = "planned_time")
    private LocalDateTime plannedTime;

    @Column(name = "actual_time")
    private LocalDateTime actualTime;
}