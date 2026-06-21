package com.badier.badier_ride.repository;

import com.badier.badier_ride.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRouteIdOrderBySentAtAsc(Long routeId);
    long countByRouteIdAndIsReadFalseAndSenderUsernameNot(Long routeId, String username);
}
