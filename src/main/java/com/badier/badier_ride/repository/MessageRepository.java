package com.badier.badier_ride.repository;

import com.badier.badier_ride.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRouteIdOrderBySentAtAsc(Long routeId);
    long countByRouteIdAndIsReadFalseAndSenderUsernameNot(Long routeId, String username);

    @Query("SELECT m.route.id, COUNT(m) FROM Message m WHERE m.route.id IN :routeIds AND m.isRead = false AND m.sender.username <> :username GROUP BY m.route.id")
    List<Object[]> countUnreadByRouteIds(@Param("routeIds") List<Long> routeIds, @Param("username") String username);
}
