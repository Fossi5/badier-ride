package com.badier.badier_ride.entity;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "admins")
public class Admin extends User {
    
    @Column(name = "department")
    private String department;
    
    // Un admin peut être responsable de plusieurs configurations système
    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private List<SystemConfig> configurations;
}