package com.badier.badier_ride.repository;

import com.badier.badier_ride.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverUsernameOrderByCreatedAtDesc(String username);
    List<Notification> findByReceiverUsernameAndIsReadFalse(String username);
    long countByReceiverUsernameAndIsReadFalse(String username);
}
