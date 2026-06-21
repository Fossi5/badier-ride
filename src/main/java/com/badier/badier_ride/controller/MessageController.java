package com.badier.badier_ride.controller;

import com.badier.badier_ride.dto.MessageRequest;
import com.badier.badier_ride.dto.MessageResponse;
import com.badier.badier_ride.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes/{routeId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable Long routeId, Principal principal) {
        return ResponseEntity.ok(messageService.getMessages(routeId, principal.getName()));
    }

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable Long routeId,
                                                       @Valid @RequestBody MessageRequest request,
                                                       Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.sendMessage(routeId, request, principal.getName()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(@PathVariable Long routeId, Principal principal) {
        return ResponseEntity.ok(Map.of("count", messageService.countUnread(routeId, principal.getName())));
    }
}
