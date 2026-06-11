package com.badier.badier_ride.repository;

import com.badier.badier_ride.entity.Alert;
import com.badier.badier_ride.enumeration.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByStatusOrderByCreatedAtDesc(AlertStatus status);
    List<Alert> findByDispatcherUsernameOrderByCreatedAtDesc(String username);
}
