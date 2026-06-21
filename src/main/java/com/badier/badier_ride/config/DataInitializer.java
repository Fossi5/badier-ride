package com.badier.badier_ride.config;

import com.badier.badier_ride.entity.*;
import com.badier.badier_ride.enumeration.*;
import com.badier.badier_ride.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        // ── Utilisateurs ──────────────────────────────────────────────────────

        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setEmail("admin@badierride.com");
        admin.setPassword(passwordEncoder.encode("Pass123!"));
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        User savedAdmin = userRepository.save(admin);

        Dispatcher dispatcher1 = dispatcher("dispatcher1", "sophie.martin@badierride.com", "Logistique Nord");
        Dispatcher dispatcher2 = dispatcher("dispatcher2", "marc.dupont@badierride.com", "Logistique Sud");
        dispatcherRepository.saveAll(List.of(dispatcher1, dispatcher2));

        Driver driver1 = driver("driver1", "jean.durand@badierride.com", "+32 470 11 22 33", "Camionnette");
        Driver driver2 = driver("driver2", "alice.leroy@badierride.com", "+32 470 44 55 66", "Vélo cargo");
        Driver driver3 = driver("driver3", "thomas.bernard@badierride.com", "+32 470 77 88 99", "Camionnette");
        Driver driver4 = driver("driver4", "camille.moreau@badierride.com", "+32 470 12 34 56", "Scooter électrique");
        driverRepository.saveAll(List.of(driver1, driver2, driver3, driver4));

        // ── Adresses — Bruxelles & environs (coordonnées réelles) ────────────

        Address a01 = addr("12 Rue de la Loi",              "Bruxelles",    "1000", 50.8467, 4.3592);
        Address a02 = addr("45 Avenue Louise",               "Bruxelles",    "1050", 50.8269, 4.3647);
        Address a03 = addr("8 Place du Grand Sablon",        "Bruxelles",    "1000", 50.8426, 4.3533);
        Address a04 = addr("23 Chaussée de Waterloo",        "Ixelles",      "1050", 50.8207, 4.3615);
        Address a05 = addr("67 Rue Neuve",                   "Bruxelles",    "1000", 50.8530, 4.3537);
        Address a06 = addr("3 Rue du Lombard",               "Bruxelles",    "1000", 50.8460, 4.3490);
        Address a07 = addr("19 Avenue de Tervueren",         "Etterbeek",    "1040", 50.8443, 4.3924);
        Address a08 = addr("55 Rue Haute",                   "Bruxelles",    "1000", 50.8392, 4.3471);
        Address a09 = addr("100 Boulevard du Midi",          "Saint-Gilles", "1060", 50.8324, 4.3436);
        Address a10 = addr("7 Rue des Bouchers",             "Bruxelles",    "1000", 50.8483, 4.3549);
        Address a11 = addr("32 Avenue Brugmann",             "Uccle",        "1180", 50.8155, 4.3561);
        Address a12 = addr("14 Rue Vanderkindere",           "Uccle",        "1180", 50.8104, 4.3604);
        Address a13 = addr("6 Place Flagey",                 "Ixelles",      "1050", 50.8280, 4.3730);
        Address a14 = addr("88 Avenue Winston Churchill",    "Uccle",        "1180", 50.8045, 4.3582);
        Address a15 = addr("2 Rue Stevin",                   "Etterbeek",    "1000", 50.8432, 4.3784);
        Address a16 = addr("41 Rue du Trône",                "Ixelles",      "1050", 50.8362, 4.3680);
        Address a17 = addr("9 Chaussée d'Ixelles",          "Ixelles",      "1050", 50.8359, 4.3617);
        Address a18 = addr("77 Rue de Laeken",               "Laeken",       "1020", 50.8632, 4.3524);
        Address a19 = addr("5 Avenue du Port",               "Molenbeek",    "1000", 50.8615, 4.3381);
        Address a20 = addr("30 Rue Royale",                  "Bruxelles",    "1000", 50.8494, 4.3614);
        addressRepository.saveAll(List.of(
                a01, a02, a03, a04, a05, a06, a07, a08, a09, a10,
                a11, a12, a13, a14, a15, a16, a17, a18, a19, a20));

        // ── Repères de temps ──────────────────────────────────────────────────

        LocalDateTime today8h   = LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime today14h  = today8h.withHour(14);
        LocalDateTime yesterday = today8h.minusDays(1);

        // ── Points de livraison ───────────────────────────────────────────────

        // Tournée 1 — Centre-ville matin (IN_PROGRESS : dp1 livré, dp2 livré, dp3 en cours, dp4-5 en attente)
        DeliveryPoint dp01 = dp(a01, "Entreprise Dupont SA",       "+32 2 123 45 67", "Fragile — ne pas retourner",           DeliveryStatus.COMPLETED,   today8h.plusHours(1),  today8h.plusHours(1).plusMinutes(15));
        DeliveryPoint dp02 = dp(a10, "Restaurant Chez Paul",       "+32 2 890 12 34", "Livrer en cuisine uniquement",         DeliveryStatus.COMPLETED,   today8h.plusHours(2),  today8h.plusHours(2).plusMinutes(8));
        DeliveryPoint dp03 = dp(a03, "Galerie d'Art Moulin",       "+32 2 345 67 89", "Œuvres d'art — extrême précaution",   DeliveryStatus.IN_PROGRESS, today8h.plusHours(3),  null);
        DeliveryPoint dp04 = dp(a06, "Brasserie du Centre",        "+32 2 567 89 01", "Colis lourd — diable obligatoire",    DeliveryStatus.PENDING,     today8h.plusHours(4),  null);
        DeliveryPoint dp05 = dp(a08, "Maison de repos Les Pins",   "+32 2 678 90 12", "Sonner à l'accueil",                  DeliveryStatus.PENDING,     today8h.plusHours(5),  null);

        // Tournée 2 — Ixelles/Uccle après-midi (PLANNED)
        DeliveryPoint dp06 = dp(a02, "Cabinet Médical Leroy",      "+32 2 234 56 78", "Appeler avant livraison",             DeliveryStatus.PENDING,     today14h.plusHours(1), null);
        DeliveryPoint dp07 = dp(a04, "Résidence Charlier",         "+32 475 11 22 33", null,                                 DeliveryStatus.PENDING,     today14h.plusHours(2), null);
        DeliveryPoint dp08 = dp(a11, "Pharmacie Brugmann",         "+32 2 344 55 66", "Médicaments — réfrigéré",            DeliveryStatus.PENDING,     today14h.plusHours(3), null);
        DeliveryPoint dp09 = dp(a12, "École Saint-Joseph",         "+32 2 376 88 11", "Remettre au secrétariat",            DeliveryStatus.PENDING,     today14h.plusHours(4), null);
        DeliveryPoint dp10 = dp(a14, "Résidence Churchill",        "+32 475 66 77 88", null,                                 DeliveryStatus.PENDING,     today14h.plusHours(5), null);

        // Tournée 3 — Etterbeek/Laeken (PLANNED)
        DeliveryPoint dp11 = dp(a07, "Ambassade de France",        "+32 2 548 87 11", "Pièce d'identité requise à l'entrée", DeliveryStatus.PENDING,    today14h,              null);
        DeliveryPoint dp12 = dp(a15, "Cabinet Stevin Avocats",     "+32 2 231 44 55", "Remettre en mains propres",          DeliveryStatus.PENDING,     today14h.plusHours(1), null);
        DeliveryPoint dp13 = dp(a18, "Entrepôt Laeken",            "+32 2 420 11 22", "Quai de déchargement — côté est",   DeliveryStatus.PENDING,     today14h.plusHours(2), null);
        DeliveryPoint dp14 = dp(a19, "Atelier Molenbeek",          "+32 2 411 33 44", "Codes d'accès : portail A3",         DeliveryStatus.PENDING,     today14h.plusHours(3), null);

        // Tournée 4 — Récurrente vélo cargo (PLANNED, récurrente quotidienne)
        DeliveryPoint dp15 = dp(a05, "Boutique Mode Élégance",     "+32 2 456 78 90", "Livraison en boutique uniquement",   DeliveryStatus.PENDING,     today8h.plusHours(2),  null);
        DeliveryPoint dp16 = dp(a13, "Marché Flagey",              "+32 2 512 99 00", "Stand n°14 — hall ouest",           DeliveryStatus.PENDING,     today8h.plusHours(3),  null);
        DeliveryPoint dp17 = dp(a16, "Librairie du Trône",         "+32 2 502 87 65", null,                                 DeliveryStatus.PENDING,     today8h.plusHours(4),  null);
        DeliveryPoint dp18 = dp(a17, "Café de la Chaussée",        "+32 2 513 22 33", "Livrer avant 10h",                  DeliveryStatus.PENDING,     today8h.plusHours(1),  null);

        // Tournée historique hier — COMPLETED
        DeliveryPoint dp19 = dp(a20, "Ministère des Finances",     "+32 2 257 71 11", null,                                 DeliveryStatus.COMPLETED,   yesterday.plusHours(1), yesterday.plusHours(1).plusMinutes(10));
        DeliveryPoint dp20 = dp(a09, "Pharmacie du Midi",          "+32 2 789 01 23", "Médicaments — réfrigéré",           DeliveryStatus.COMPLETED,   yesterday.plusHours(2), yesterday.plusHours(2).plusMinutes(6));
        DeliveryPoint dp21 = dp(a02, "Clinique Sainte-Anne",       "+32 2 764 31 11", "Entrée urgences — bâtiment C",     DeliveryStatus.FAILED,      yesterday.plusHours(3), null);

        deliveryPointRepository.saveAll(List.of(
                dp01, dp02, dp03, dp04, dp05,
                dp06, dp07, dp08, dp09, dp10,
                dp11, dp12, dp13, dp14,
                dp15, dp16, dp17, dp18,
                dp19, dp20, dp21));

        // ── Tournées ──────────────────────────────────────────────────────────

        Route route1 = routeRepository.save(Route.builder()
                .name("Centre-Ville Matin")
                .driver(driver1)
                .dispatcher(dispatcher1)
                .status(RouteStatus.IN_PROGRESS)
                .startTime(today8h)
                .notes("Priorité aux livraisons fragiles. Stationnement payant — badge fourni.")
                .recurring(false)
                .build());

        Route route2 = routeRepository.save(Route.builder()
                .name("Ixelles — Uccle Après-midi")
                .driver(driver3)
                .dispatcher(dispatcher1)
                .status(RouteStatus.PLANNED)
                .startTime(today14h)
                .notes("Zones résidentielles — sonner et attendre 2 minutes avant de laisser avis de passage.")
                .recurring(false)
                .build());

        Route route3 = routeRepository.save(Route.builder()
                .name("Etterbeek — Laeken")
                .driver(driver4)
                .dispatcher(dispatcher2)
                .status(RouteStatus.PLANNED)
                .startTime(today14h)
                .notes("Accès ambassade : prévoir la pièce d'identité du chauffeur.")
                .recurring(false)
                .build());

        Route route4 = routeRepository.save(Route.builder()
                .name("Livraisons Vélo Cargo Centre")
                .driver(driver2)
                .dispatcher(dispatcher2)
                .status(RouteStatus.PLANNED)
                .startTime(today8h)
                .notes("Vélo cargo — éviter les grands boulevards aux heures de pointe.")
                .recurring(true)
                .recurrenceType(RecurrenceType.DAILY)
                .recurrenceEndDate(LocalDate.now().plusMonths(1))
                .build());

        Route route5 = routeRepository.save(Route.builder()
                .name("Tournée Centre — Hier")
                .driver(driver1)
                .dispatcher(dispatcher1)
                .status(RouteStatus.COMPLETED)
                .startTime(yesterday)
                .endTime(yesterday.plusHours(3).plusMinutes(45))
                .notes(null)
                .recurring(false)
                .build());

        // ── RouteDeliveryPoints ───────────────────────────────────────────────

        routeDeliveryPointRepository.saveAll(List.of(
                // Route 1 — IN_PROGRESS
                rdp(route1, dp01, 1, true,  false, DeliveryStatus.COMPLETED,   today8h.plusHours(1),  today8h.plusHours(1).plusMinutes(15)),
                rdp(route1, dp02, 2, false, false, DeliveryStatus.COMPLETED,   today8h.plusHours(2),  today8h.plusHours(2).plusMinutes(8)),
                rdp(route1, dp03, 3, false, false, DeliveryStatus.IN_PROGRESS, today8h.plusHours(3),  null),
                rdp(route1, dp04, 4, false, false, DeliveryStatus.PENDING,     today8h.plusHours(4),  null),
                rdp(route1, dp05, 5, false, true,  DeliveryStatus.PENDING,     today8h.plusHours(5),  null),

                // Route 2 — PLANNED
                rdp(route2, dp06, 1, true,  false, DeliveryStatus.PENDING,     today14h.plusHours(1), null),
                rdp(route2, dp07, 2, false, false, DeliveryStatus.PENDING,     today14h.plusHours(2), null),
                rdp(route2, dp08, 3, false, false, DeliveryStatus.PENDING,     today14h.plusHours(3), null),
                rdp(route2, dp09, 4, false, false, DeliveryStatus.PENDING,     today14h.plusHours(4), null),
                rdp(route2, dp10, 5, false, true,  DeliveryStatus.PENDING,     today14h.plusHours(5), null),

                // Route 3 — PLANNED
                rdp(route3, dp11, 1, true,  false, DeliveryStatus.PENDING,     today14h,              null),
                rdp(route3, dp12, 2, false, false, DeliveryStatus.PENDING,     today14h.plusHours(1), null),
                rdp(route3, dp13, 3, false, false, DeliveryStatus.PENDING,     today14h.plusHours(2), null),
                rdp(route3, dp14, 4, false, true,  DeliveryStatus.PENDING,     today14h.plusHours(3), null),

                // Route 4 — PLANNED, récurrente
                rdp(route4, dp18, 1, true,  false, DeliveryStatus.PENDING,     today8h.plusHours(1),  null),
                rdp(route4, dp15, 2, false, false, DeliveryStatus.PENDING,     today8h.plusHours(2),  null),
                rdp(route4, dp16, 3, false, false, DeliveryStatus.PENDING,     today8h.plusHours(3),  null),
                rdp(route4, dp17, 4, false, true,  DeliveryStatus.PENDING,     today8h.plusHours(4),  null),

                // Route 5 — COMPLETED (historique hier)
                rdp(route5, dp19, 1, true,  false, DeliveryStatus.COMPLETED,   yesterday.plusHours(1), yesterday.plusHours(1).plusMinutes(10)),
                rdp(route5, dp20, 2, false, false, DeliveryStatus.COMPLETED,   yesterday.plusHours(2), yesterday.plusHours(2).plusMinutes(6)),
                rdp(route5, dp21, 3, false, true,  DeliveryStatus.FAILED,      yesterday.plusHours(3), null)
        ));

        // ── Notifications ─────────────────────────────────────────────────────

        notificationRepository.saveAll(List.of(
                notif(savedAdmin,    driver1,     NotificationType.NEW_ROUTE,    "Tournée « " + route1.getName() + " » démarrée — bonne route !"),
                notif(dispatcher1,   driver3,     NotificationType.NEW_ROUTE,    "Nouvelle tournée assignée : " + route2.getName()),
                notif(dispatcher2,   driver4,     NotificationType.NEW_ROUTE,    "Nouvelle tournée assignée : " + route3.getName()),
                notif(dispatcher2,   driver2,     NotificationType.NEW_ROUTE,    "Tournée récurrente activée : " + route4.getName()),
                notif(dispatcher1,   driver1,     NotificationType.ROUTE_UPDATE, "Point n°3 (Galerie Moulin) : client demande rappel avant arrivée"),
                notif(savedAdmin,    dispatcher1, NotificationType.SYSTEM,       "Maintenance planifiée samedi 22h–24h — sauvegarder vos données"),
                notif(savedAdmin,    dispatcher2, NotificationType.SYSTEM,       "Maintenance planifiée samedi 22h–24h — sauvegarder vos données"),
                notif(dispatcher1,   driver1,     NotificationType.ROUTE_UPDATE, "Livraison n°2 (Restaurant Chez Paul) confirmée par le client")
        ));
    }

    // ── Builders privés ───────────────────────────────────────────────────────

    private Dispatcher dispatcher(String username, String email, String department) {
        Dispatcher d = new Dispatcher();
        d.setUsername(username);
        d.setEmail(email);
        d.setPassword(passwordEncoder.encode("Pass123!"));
        d.setRole(UserRole.DISPATCHER);
        d.setActive(true);
        d.setDepartment(department);
        return d;
    }

    private Driver driver(String username, String email, String phone, String vehicle) {
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

    private Address addr(String street, String city, String postal, double lat, double lng) {
        return Address.builder()
                .street(street).city(city).postalCode(postal).country("Belgique")
                .latitude(lat).longitude(lng).isVerified(true)
                .build();
    }

    private DeliveryPoint dp(Address address, String client, String phone, String note,
                              DeliveryStatus status, LocalDateTime planned, LocalDateTime actual) {
        return DeliveryPoint.builder()
                .address(address).clientName(client).clientPhone(phone).deliveryNote(note)
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
