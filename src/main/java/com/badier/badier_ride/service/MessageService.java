package com.badier.badier_ride.service;

import com.badier.badier_ride.dto.MessageRequest;
import com.badier.badier_ride.dto.MessageResponse;
import com.badier.badier_ride.entity.Message;
import com.badier.badier_ride.entity.Route;
import com.badier.badier_ride.entity.User;
import com.badier.badier_ride.enumeration.NotificationType;
import com.badier.badier_ride.enumeration.RouteStatus;
import com.badier.badier_ride.exception.InvalidOperationException;
import com.badier.badier_ride.exception.ResourceNotFoundException;
import com.badier.badier_ride.exception.UnauthorizedOperationException;
import com.badier.badier_ride.repository.MessageRepository;
import com.badier.badier_ride.repository.RouteRepository;
import com.badier.badier_ride.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public List<MessageResponse> getMessages(Long routeId, String username) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournée introuvable"));
        checkAccess(route, username);

        messageRepository.findByRouteIdOrderBySentAtAsc(routeId).forEach(m -> {
            if (!m.getSender().getUsername().equals(username) && !m.getIsRead()) {
                m.setIsRead(true);
                messageRepository.save(m);
            }
        });

        return messageRepository.findByRouteIdOrderBySentAtAsc(routeId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public MessageResponse sendMessage(Long routeId, MessageRequest request, String username) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournée introuvable"));

        if (route.getStatus() == RouteStatus.COMPLETED || route.getStatus() == RouteStatus.CANCELLED) {
            throw new InvalidOperationException("La messagerie est fermée pour cette tournée");
        }

        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        checkAccess(route, username);

        Message message = Message.builder()
                .content(request.getContent())
                .sender(sender)
                .route(route)
                .build();

        MessageResponse response = mapToResponse(messageRepository.save(message));

        String senderRole = sender.getRole().name();
        String notifText = "Nouveau message sur la tournée \"" + route.getName() + "\"";
        Long receiverId = null;
        if (senderRole.equals("DRIVER") && route.getDispatcher() != null) {
            receiverId = route.getDispatcher().getId();
        } else if ((senderRole.equals("DISPATCHER") || senderRole.equals("ADMIN")) && route.getDriver() != null) {
            receiverId = route.getDriver().getId();
        }

        if (receiverId != null) {
            final Long finalReceiverId = receiverId;
            userRepository.findById(finalReceiverId).ifPresent(receiver ->
                notificationService.send(receiver, sender, NotificationType.MESSAGE, notifText)
            );
        }

        return response;
    }

    public long countUnread(Long routeId, String username) {
        return messageRepository.countByRouteIdAndIsReadFalseAndSenderUsernameNot(routeId, username);
    }

    public Map<Long, Long> countUnreadBulk(List<Long> routeIds, String username) {
        Map<Long, Long> result = new HashMap<>();
        if (routeIds == null || routeIds.isEmpty()) return result;
        messageRepository.countUnreadByRouteIds(routeIds, username)
                .forEach(row -> result.put((Long) row[0], (Long) row[1]));
        return result;
    }

    private void checkAccess(Route route, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        String role = user.getRole().name();
        if (role.equals("ADMIN")) return;
        boolean isDriver = route.getDriver() != null && route.getDriver().getUsername().equals(username);
        boolean isDispatcher = route.getDispatcher() != null && route.getDispatcher().getUsername().equals(username);
        if (!isDriver && !isDispatcher) {
            throw new UnauthorizedOperationException("Accès refusé à cette messagerie");
        }
    }

    private MessageResponse mapToResponse(Message m) {
        return MessageResponse.builder()
                .id(m.getId())
                .content(m.getContent())
                .senderUsername(m.getSender().getUsername())
                .senderRole(m.getSender().getRole().name())
                .routeId(m.getRoute().getId())
                .sentAt(m.getSentAt())
                .isRead(m.getIsRead())
                .build();
    }
}
