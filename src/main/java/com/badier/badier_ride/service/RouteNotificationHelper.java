package com.badier.badier_ride.service;

import com.badier.badier_ride.entity.Route;
import com.badier.badier_ride.enumeration.NotificationType;
import com.badier.badier_ride.enumeration.RouteStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RouteNotificationHelper {

    private final NotificationService notificationService;
    private final EmailService emailService;

    public void notifyRouteCreated(Route route) {
        notificationService.send(
            route.getDriver(),
            route.getDispatcher(),
            NotificationType.NEW_ROUTE,
            "Nouvelle tournée assignée : " + route.getName()
        );
        if (route.getDriver().getEmail() != null) {
            emailService.send(
                route.getDriver().getEmail(),
                "Nouvelle tournée assignée",
                "Bonjour " + route.getDriver().getUsername() + ",\n\n" +
                "Une nouvelle tournée vous a été assignée : " + route.getName() + "\n" +
                "Heure de départ prévue : " + (route.getStartTime() != null ? route.getStartTime() : "Non définie") + "\n\n" +
                "Connectez-vous à Badier Ride pour voir les détails."
            );
        }
    }

    public void notifyRouteUpdated(Route route, String newDriverEmail, String newDriverUsername) {
        notificationService.send(
            route.getDriver(),
            route.getDispatcher(),
            NotificationType.ROUTE_UPDATE,
            "La tournée \"" + route.getName() + "\" a été modifiée"
        );
        if (newDriverEmail != null) {
            emailService.send(
                newDriverEmail,
                "Nouvelle tournée assignée",
                "Bonjour " + newDriverUsername + ",\n\n" +
                "Une nouvelle tournée vous a été assignée : " + route.getName() + "\n" +
                "Heure de départ prévue : " + (route.getStartTime() != null ? route.getStartTime() : "Non définie") + "\n\n" +
                "Connectez-vous à Badier Ride pour voir les détails."
            );
        }
    }

    public void notifyStatusChanged(Route route, RouteStatus status) {
        String statusMsg = switch (status) {
            case IN_PROGRESS -> "démarrée";
            case COMPLETED   -> "terminée";
            case CANCELLED   -> "annulée";
            default          -> "mise à jour";
        };
        notificationService.send(
            route.getDispatcher(),
            route.getDriver(),
            NotificationType.ROUTE_UPDATE,
            "Tournée \"" + route.getName() + "\" : " + statusMsg
        );
    }
}
