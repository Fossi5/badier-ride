package com.badier.badier_ride.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "postal_code", nullable = false)
    @Pattern(regexp = "\\d{4}", message = "Le code postal doit contenir exactement 4 chiffres")
    private String postalCode;

    private String country;

    private Double latitude;

    private Double longitude;

    @Column(name = "is_verified")
    private Boolean isVerified = false;
}