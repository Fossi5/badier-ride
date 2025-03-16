package com.badier.badier_ride.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dispatchers")
public class Dispatcher extends User {
    
    @Column(name = "department")
    private String department;
    
    // Un dispatcher peut gérer plusieurs routes
    @OneToMany(mappedBy = "dispatcher")
    private List<Route> managedRoutes;
    
    // Un dispatcher peut gérer plusieurs alertes
    @OneToMany(mappedBy = "dispatcher")
    private List<Alert> managedAlerts;
}


