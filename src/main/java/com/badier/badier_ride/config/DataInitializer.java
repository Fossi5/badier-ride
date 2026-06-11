package com.badier.badier_ride.config;

import com.badier.badier_ride.entity.*;
import com.badier.badier_ride.enumeration.*;
import com.badier.badier_ride.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final DispatcherRepository dispatcherRepository;
    private final AddressRepository addressRepository;
    private final DeliveryPointRepository deliveryPointRepository;
    private final RouteRepository routeRepository;
    private final RouteDeliveryPointRepository routeDeliveryPointRepository;
    private final NotificationRepository notificationRepository;
    private final AlertRepository alertRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByUsername("admin")) return;

        // ── Utilisateurs ──────────────────────────────────────────────────────

        User admin = userRepository.save(User.builder()
                .username("admin")
                .email("admin@badierride.com")
                .password(passwordEncoder.encode("Pass123!"))
                .role(UserRole.ADMIN)
                .active(true)
                .build());

        Dispatcher dispatcher1 = buildDispatcher("dispatcher1", "sophie.martin@badierride.com",
                "Sophie Martin", "Logistique Nord");
        Dispatcher dispatcher2 = buildDispatcher("dispatcher2", "marc.dupont@badierride.com",
                "Marc Dupont", "Logistique Sud");
        dispatcherRepository.saveAll(List.of(dispatcher1, dispatcher2));

        Driver driver1 = buildDriver("driver1", "jean.durand@badierride.com",
                "Jean Durand", "+32 470 11 22 33", "Camionnette");
        Driver driver2 = buildDriver("driver2", "alice.leroy@badierride.com",
                "Alice Leroy", "+32 470 44 55 66", "Vélo cargo");
        Driver driver3 = buildDriver("driver3", "thomas.bernard@badierride.com",
                "Thomas Bernard", "+32 470 77 88 99", "Camionnette");
        driverRepository.saveAll(List.of(driver1, driver2, driver3));

        // ── Adresses (coordonnées réelles — Bruxelles & environs) ─────────────

        Address a1 = addr("12 Rue de la Loi", "Bruxelles", "1000", "Belgique", 50.8467, 4.3592);
        Address a2 = addr("45 Avenue Louise", "Bruxelles", "1050", "Belgique", 50.8269, 4.3647);
        Address a3 = addr("8 Place du Grand Sablon", "Bruxelles", "1000", "Belgique", 50.8426, 4.3533);
        Address a4 = addr("23 Chaussée de Waterloo", "Ixelles", "1050", "Belgique", 50.8207, 4.3615);
        Address a5 = addr("67 Rue Neuve", "Bruxelles", "1000", "Belgique", 50.8530, 4.3537);
        Address a6 = addr("3 Rue du Lombard", "Bruxelles", "1000", "Belgique", 50.8460, 4.3490);
        Address a7 = addr("19 Avenue de Tervueren", "Etterbeek", "1040", "Belgique", 50.8443, 4.3924);
        Address a8 = addr("55 Rue Haute", "Bruxelles", "1000", "Belgique", 50.8392, 4.3471);
        Address a9 = addr("100 Boulevard du Midi", "Saint-Gilles", "1060", "Belgique", 50.8324, 4.3436);
        Address a10 = addr("7 Rue des Bouchers", "Bruxelles", "1000", "Belgique", 50.8483, 4.3549);
        addressRepository.saveAll(List.of(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10));

        // ── Points de livraison ───────────────────────────────────────────────

        LocalDateTime baseTime = LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0);

        DeliveryPoint dp1 = dp(a1, "Entreprise Dupont SA", "+32 2 123 45 67", "Fragile — ne pas retourner", DeliveryStatus.COMPLETED, baseTime.plusHours(1), baseTime.plusHours(1).plusMinutes(12));
        DeliveryPoint dp2 = dp(a2, "Cabinet Médical Leroy", "+32 2 234 56 78", "Appeler avant livraison", DeliveryStatus.COMPLETED, baseTime.plusHours(2), baseTime.plusHours(2).plusMinutes(5));
        DeliveryPoint dp3 = dp(a3, "Galerie d'Art Moulin", "+32 2 345 67 89", "Œuvres d'art — extrême précaution", DeliveryStatus.IN_PROGRESS, baseTime.plusHours(3), null);
        DeliveryPoint dp4 = dp(a4, "Résidence Charlier", "+32 475 11 22 33", null, DeliveryStatus.PENDING, baseTime.plusHours(4), null);
        DeliveryPoint dp5 = dp(a5, "Boutique Mode Élégance", "+32 2 456 78 90", "Livraison en boutique uniquement", DeliveryStatus.PENDING, baseTime.plusHours(5), null);
        DeliveryPoint dp6 = dp(a6, "Brasserie du Centre", "+32 2 567 89 01", "Colis lourd — diable obligatoire", DeliveryStatus.PENDING, baseTime.plusHours(6), null);
        DeliveryPoint dp7 = dp(a7, "Ambassade de France", "+32 2 548 87 11", "Pièce d'identité requise", DeliveryStatus.FAILED, baseTime.minusDays(1).plusHours(10), null);
        DeliveryPoint dp8 = dp(a8, "Maison de repos Les Pins", "+32 2 678 90 12", "Sonner à l'accueil", DeliveryStatus.PENDING, baseTime.plusHours(7), null);
        DeliveryPoint dp9 = dp(a9, "Pharmacie du Midi", "+32 2 789 01 23", "Médicaments — réfrigéré", DeliveryStatus.PENDING, baseTime.plusHours(8), null);
        DeliveryPoint dp10 = dp(a10, "Restaurant Chez Paul", "+32 2 890 12 34", null, DeliveryStatus.PENDING, baseTime.plusHours(9), null);
        deliveryPointRepository.saveAll(List.of(dp1, dp2, dp3, dp4, dp5, dp6, dp7, dp8, dp9, dp10));

        // ── Tournées ──────────────────────────────────────────────────────────

        Route route1 = routeRepository.save(Route.builder()
                .name("Tournée Centre-Ville Matin")
                .driver(driver1)
                .dispatcher(dispatcher1)
                .status(RouteStatus.IN_PROGRESS)
                .startTime(baseTime)
                .notes("Priorité aux livraisons fragiles")
                .recurring(false)
                .build());

        Route route2 = routeRepository.save(Route.builder()
                .name("Tournée Ixelles Midi")
                .driver(driver2)
                .dispatcher(dispatcher1)
                .status(RouteStatus.PLANNED)
                .startTime(baseTime.plusHours(4))
                .notes(null)
                .recurring(true)
                .recurrenceType(RecurrenceType.DAILY)
                .recurrenceEndDate(LocalDateTime.now().plusMonths(1).toLocalDate())
                .build());

        Route route3 = routeRepository.save(Route.builder()
                .name("Tournée Etterbeek Après-midi")
                .driver(driver3)
                .dispatcher(dispatcher2)
                .status(RouteStatus.PLANNED)
                .startTime(baseTime.plusHours(6))
                .notes("Zones de stationnement limitées")
                .recurring(false)
                .build());

        Route route4 = routeRepository.save(Route.builder()
                .name("Tournée Historique Hier")
                .driver(driver1)
                .dispatcher(dispatcher2)
                .status(RouteStatus.COMPLETED)
                .startTime(baseTime.minusDays(1))
                .endTime(baseTime.minusDays(1).plusHours(4))
                .notes(null)
                .recurring(false)
                .build());

        // ── Association tournées ↔ points de livraison ────────────────────────

        routeDeliveryPointRepository.saveAll(List.of(
                rdp(route1, dp1, 1, true, false, DeliveryStatus.COMPLETED, baseTime.plusHours(1), baseTime.plusHours(1).plusMinutes(12)),
                rdp(route1, dp2, 2, false, false, DeliveryStatus.COMPLETED, baseTime.plusHours(2), baseTime.plusHours(2).plusMinutes(5)),
                rdp(route1, dp3, 3, false, false, DeliveryStatus.IN_PROGRESS, baseTime.plusHours(3), null),
                rdp(route1, dp4, 4, false, true, DeliveryStatus.PENDING, baseTime.plusHours(4), null),

                rdp(route2, dp5, 1, true, false, DeliveryStatus.PENDING, baseTime.plusHours(5), null),
                rdp(route2, dp6, 2, false, false, DeliveryStatus.PENDING, baseTime.plusHours(6), null),
                rdp(route2, dp8, 3, false, true, DeliveryStatus.PENDING, baseTime.plusHours(7), null),

                rdp(route3, dp7, 1, true, false, DeliveryStatus.PENDING, baseTime.plusHours(6), null),
                rdp(route3, dp9, 2, false, false, DeliveryStatus.PENDING, baseTime.plusHours(7), null),
                rdp(route3, dp10, 3, false, true, DeliveryStatus.PENDING, baseTime.plusHours(8), null),

                rdp(route4, dp1, 1, true, false, DeliveryStatus.COMPLETED, baseTime.minusDays(1).plusHours(1), baseTime.minusDays(1).plusHours(1).plusMinutes(8)),
                rdp(route4, dp7, 2, false, true, DeliveryStatus.FAILED, baseTime.minusDays(1).plusHours(2), null)
        ));

        // ── Notifications ─────────────────────────────────────────────────────

        notificationRepository.saveAll(List.of(
                notif(dispatcher1, driver1, NotificationType.NEW_ROUTE,
                        "Nouvelle tournée assignée : " + route1.getName()),
                notif(dispatcher1, driver2, NotificationType.NEW_ROUTE,
                        "Nouvelle tournée assignée : " + route2.getName()),
                notif(dispatcher2, driver3, NotificationType.NEW_ROUTE,
                        "Nouvelle tournée assignée : " + route3.getName()),
                notif(dispatcher1, driver1, NotificationType.ROUTE_UPDATE,
                        "Tournée mise à jour — vérifiez les horaires"),
                notif(dispatcher2, driver1, NotificationType.ALERT,
                        "Alerte : retard signalé sur votre secteur"),
                notif(admin, dispatcher1, NotificationType.SYSTEM,
                        "Maintenance planifiée ce week-end")
        ));

        // ── Alertes ───────────────────────────────────────────────────────────

        Alert alert1 = new Alert();
        alert1.setTitle("Retard livraison Ambassade");
        alert1.setDescription("Le chauffeur Thomas Bernard n'a pas pu accéder au site — accès refusé sans badge.");
        alert1.setPriority(AlertPriority.HIGH);
        alert1.setStatus(AlertStatus.NEW);
        alert1.setRelatedRoute(route3);
        alert1.setDispatcher(dispatcher2);
        alert1.setDriver(driver3);
        alertRepository.save(alert1);

        Alert alert2 = new Alert();
        alert2.setTitle("Véhicule en panne");
        alert2.setDescription("Camionnette de Jean Durand immobilisée — pneu crevé. Dépannage en cours.");
        alert2.setPriority(AlertPriority.CRITICAL);
        alert2.setStatus(AlertStatus.IN_PROGRESS);
        alert2.setRelatedRoute(route1);
        alert2.setDispatcher(dispatcher1);
        alert2.setDriver(driver1);
        alertRepository.save(alert2);

        Alert alert3 = new Alert();
        alert3.setTitle("Colis endommagé");
        alert3.setDescription("Un colis destiné à la Galerie d'Art Moulin présente des dommages visibles à la réception.");
        alert3.setPriority(AlertPriority.MEDIUM);
        alert3.setStatus(AlertStatus.RESOLVED);
        alert3.setResolutionNote("Signalement effectué, remplacement commandé.");
        alert3.setDispatcher(dispatcher1);
        alertRepository.save(alert3);

        Alert alert4 = new Alert();
        alert4.setTitle("Zone embouteillée — Rue Neuve");
        alert4.setDescription("Travaux en cours, itinéraire de substitution nécessaire.");
        alert4.setPriority(AlertPriority.LOW);
        alert4.setStatus(AlertStatus.NEW);
        alert4.setDispatcher(dispatcher1);
        alertRepository.save(alert4);
    }

    // ── Builders privés ───────────────────────────────────────────────────────

    private Dispatcher buildDispatcher(String username, String email, String fullName, String department) {
        Dispatcher d = new Dispatcher();
        d.setUsername(username);
        d.setEmail(email);
        d.setPassword(passwordEncoder.encode("Pass123!"));
        d.setRole(UserRole.DISPATCHER);
        d.setActive(true);
        d.setDepartment(department);
        return d;
    }

    private Driver buildDriver(String username, String email, String fullName, String phone, String vehicle) {
        Driver d = new Driver();
        d.setUsername(username);
        d.setEmail(email);
        d.setPassword(passwordEncoder.encode("Pass123!"));
        d.setRole(UserRole.DRIVER);
        d.setActive(true);
        d.setPhoneNumber(phone);
        d.setVehicleType(vehicle);
        d.setIsAvailable(true);
        return d;
    }

    private Address addr(String street, String city, String postal, String country, double lat, double lng) {
        return Address.builder()
                .street(street).city(city).postalCode(postal).country(country)
                .latitude(lat).longitude(lng).isVerified(true)
                .build();
    }

    private DeliveryPoint dp(Address address, String client, String phone, String notes,
                              DeliveryStatus status, LocalDateTime planned, LocalDateTime actual) {
        return DeliveryPoint.builder()
                .address(address).clientName(client).clientPhone(phone).notes(notes)
                .status(status).plannedTime(planned).actualTime(actual)
                .build();
    }

    private RouteDeliveryPoint rdp(Route route, DeliveryPoint point, int order,
                                    boolean isStart, boolean isEnd, DeliveryStatus status,
                                    LocalDateTime planned, LocalDateTime actual) {
        return RouteDeliveryPoint.builder()
                .route(route).deliveryPoint(point).sequenceOrder(order)
                .isStartPoint(isStart).isEndPoint(isEnd)
                .status(status).plannedTime(planned).actualTime(actual)
                .build();
    }

    private Notification notif(User sender, User receiver, NotificationType type, String message) {
        Notification n = new Notification();
        n.setSender(sender);
        n.setReceiver(receiver);
        n.setType(type);
        n.setMessage(message);
        n.setIsRead(false);
        return n;
    }
}
