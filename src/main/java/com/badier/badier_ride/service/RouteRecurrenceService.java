package com.badier.badier_ride.service;

import com.badier.badier_ride.entity.Route;
import com.badier.badier_ride.entity.RouteDeliveryPoint;
import com.badier.badier_ride.enumeration.RouteStatus;
import com.badier.badier_ride.repository.RouteRepository;
import com.badier.badier_ride.repository.RouteDeliveryPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteRecurrenceService {

    private final RouteRepository routeRepository;
    private final RouteDeliveryPointRepository routeDeliveryPointRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "${app.recurrence.cron:0 0 6 * * *}")
    @Transactional
    public void generateRecurringRoutes() {
        LocalDate today = LocalDate.now();
        List<Route> recurringRoutes = routeRepository.findByRecurringTrue();

        for (Route source : recurringRoutes) {
            if (source.getRecurrenceEndDate() != null && today.isAfter(source.getRecurrenceEndDate())) {
                continue;
            }
            if (!shouldGenerateToday(source, today)) {
                continue;
            }
            duplicateRoute(source, today);
        }
    }

    private boolean shouldGenerateToday(Route route, LocalDate today) {
        if (route.getRecurrenceType() == null) return false;
        return switch (route.getRecurrenceType()) {
            case DAILY -> true;
            case WEEKLY -> route.getStartTime() != null &&
                route.getStartTime().getDayOfWeek() == today.getDayOfWeek();
            case MONTHLY -> route.getStartTime() != null &&
                route.getStartTime().getDayOfMonth() == today.getDayOfMonth();
        };
    }

    private void duplicateRoute(Route source, LocalDate date) {
        Route newRoute = Route.builder()
                .name(source.getName())
                .driver(source.getDriver())
                .dispatcher(source.getDispatcher())
                .status(RouteStatus.PLANNED)
                .notes(source.getNotes())
                .recurring(false)
                .startTime(source.getStartTime() != null
                    ? date.atTime(source.getStartTime().toLocalTime())
                    : date.atStartOfDay())
                .build();

        Route saved = routeRepository.save(newRoute);

        List<RouteDeliveryPoint> points = routeDeliveryPointRepository.findByRouteId(source.getId());
        for (RouteDeliveryPoint rdp : points) {
            RouteDeliveryPoint copy = RouteDeliveryPoint.builder()
                    .route(saved)
                    .deliveryPoint(rdp.getDeliveryPoint())
                    .sequenceOrder(rdp.getSequenceOrder())
                    .isStartPoint(rdp.getIsStartPoint())
                    .isEndPoint(rdp.getIsEndPoint())
                    .plannedTime(rdp.getPlannedTime() != null
                        ? date.atTime(rdp.getPlannedTime().toLocalTime())
                        : null)
                    .build();
            routeDeliveryPointRepository.save(copy);
        }

        if (saved.getDriver() != null && saved.getDispatcher() != null) {
            notificationService.send(
                saved.getDriver(),
                saved.getDispatcher(),
                com.badier.badier_ride.enumeration.NotificationType.NEW_ROUTE,
                "Tournée récurrente planifiée : " + saved.getName()
            );
        }
    }
}
