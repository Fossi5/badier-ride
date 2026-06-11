package com.badier.badier_ride.service;

import com.badier.badier_ride.dto.NotificationResponse;
import com.badier.badier_ride.entity.Notification;
import com.badier.badier_ride.entity.User;
import com.badier.badier_ride.enumeration.NotificationType;
import com.badier.badier_ride.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void send(User receiver, User sender, NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setReceiver(receiver);
        notification.setSender(sender);
        notification.setType(type);
        notification.setMessage(message);
        notification.setIsRead(false);
        notificationRepository.save(notification);
    }

    public List<NotificationResponse> getForUser(String username) {
        return notificationRepository.findByReceiverUsernameOrderByCreatedAtDesc(username)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public long countUnread(String username) {
        return notificationRepository.countByReceiverUsernameAndIsReadFalse(username);
    }

    public void markAllRead(String username) {
        notificationRepository.findByReceiverUsernameAndIsReadFalse(username).forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(LocalDateTime.now());
            notificationRepository.save(n);
        });
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .message(n.getMessage())
                .type(n.getType().name())
                .senderUsername(n.getSender() != null ? n.getSender().getUsername() : null)
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
