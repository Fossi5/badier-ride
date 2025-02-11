package com.badier.badier_ride.entity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    private String country;

    private Double latitude;

    private Double longitude;

    @Column(name = "is_verified")
    private Boolean isVerified = false;
}