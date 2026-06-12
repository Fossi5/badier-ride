package com.badier.badier_ride.service;

import com.badier.badier_ride.dto.AlertRequest;
import com.badier.badier_ride.dto.AlertResponse;
import com.badier.badier_ride.entity.Alert;
import com.badier.badier_ride.entity.Dispatcher;
import com.badier.badier_ride.entity.Driver;
import com.badier.badier_ride.entity.Route;
import com.badier.badier_ride.enumeration.AlertPriority;
import com.badier.badier_ride.enumeration.AlertStatus;
import com.badier.badier_ride.enumeration.NotificationType;
import com.badier.badier_ride.exception.ResourceNotFoundException;
import com.badier.badier_ride.repository.AlertRepository;
import com.badier.badier_ride.repository.DispatcherRepository;
import com.badier.badier_ride.repository.DriverRepository;
import com.badier.badier_ride.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final DispatcherRepository dispatcherRepository;
    private final DriverRepository driverRepository;
    private final RouteRepository routeRepository;
    private final NotificationService notificationService;

    public AlertResponse createAlert(AlertRequest request, String dispatcherUsername) {
        Dispatcher dispatcher = dispatcherRepository.findByUsername(dispatcherUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatcher not found: " + dispatcherUsername));

        Alert alert = new Alert();
        alert.setTitle(request.getTitle());
        alert.setDescription(request.getDescription());
        alert.setPriority(AlertPriority.valueOf(request.getPriority()));
        alert.setStatus(AlertStatus.NEW);
        alert.setDispatcher(dispatcher);

        if (request.getRouteId() != null) {
            Route route = routeRepository.findById(request.getRouteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Route not found: " + request.getRouteId()));
            alert.setRelatedRoute(route);
        }

        Driver driver = null;
        if (request.getDriverId() != null) {
            driver = driverRepository.findById(request.getDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + request.getDriverId()));
            alert.setDriver(driver);
        }

        Alert saved = alertRepository.save(alert);

        String priorityLabel = switch (saved.getPriority()) {
            case LOW      -> "Faible";
            case MEDIUM   -> "Moyen";
            case HIGH     -> "Élevé";
            case CRITICAL -> "Critique";
        };
        String message = "[" + priorityLabel + "] " + saved.getTitle();

        if (driver != null) {
            notificationService.send(driver, dispatcher, NotificationType.ALERT, message);
        }

        return mapToResponse(saved);
    }

    public List<AlertResponse> getAllAlerts() {
        return alertRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<AlertResponse> getAlertsByStatus(AlertStatus status) {
        return alertRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public AlertResponse updateAlertStatus(Long id, String status) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + id));
        alert.setStatus(AlertStatus.valueOf(status));
        return mapToResponse(alertRepository.save(alert));
    }

    public AlertResponse resolveAlert(Long id, String resolutionNote) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + id));
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolutionNote(resolutionNote);
        alert.setResolvedAt(LocalDateTime.now());
        return mapToResponse(alertRepository.save(alert));
    }

    private AlertResponse mapToResponse(Alert a) {
        return AlertResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .description(a.getDescription())
                .priority(a.getPriority().name())
                .status(a.getStatus().name())
                .dispatcherUsername(a.getDispatcher() != null ? a.getDispatcher().getUsername() : null)
                .driverUsername(a.getDriver() != null ? a.getDriver().getUsername() : null)
                .createdAt(a.getCreatedAt())
                .resolvedAt(a.getResolvedAt())
                .resolutionNote(a.getResolutionNote())
                .build();
    }
}
