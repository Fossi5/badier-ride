package com.badier.badier_ride.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "drivers")
public class Driver extends User {
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "vehicle_type")
    private String vehicleType;
    
    @Column(name = "is_available")
    private Boolean isAvailable = true;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "current_location_id")
    private Location currentLocation;
}