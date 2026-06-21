package com.badier.badier_ride.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.badier.badier_ride.enumeration.DeliveryStatus;
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "client_email")
    private String clientEmail;

    @Column(name = "client_note", columnDefinition = "TEXT")
    private String clientNote;

    @Column(name = "delivery_note", columnDefinition = "TEXT")
    private String deliveryNote;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @Column(name = "planned_time")
    private LocalDateTime plannedTime;

    @Column(name = "actual_time")
    private LocalDateTime actualTime;
}