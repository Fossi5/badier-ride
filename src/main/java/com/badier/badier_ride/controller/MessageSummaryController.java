package com.badier.badier_ride.controller;

import com.badier.badier_ride.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageSummaryController {

    private final MessageService messageService;

    @GetMapping("/unread-counts")
    public ResponseEntity<Map<Long, Long>> unreadCounts(
            @RequestParam List<Long> routeIds,
            Principal principal) {
        return ResponseEntity.ok(messageService.countUnreadBulk(routeIds, principal.getName()));
    }
}
