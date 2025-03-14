package com.badier.badier_ride.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.badier.badier_ride.dto.DeliveryPointResponse;
import com.badier.badier_ride.dto.RouteRequest;
import com.badier.badier_ride.dto.RouteResponse;
import com.badier.badier_ride.dto.UserSummaryResponse;
import com.badier.badier_ride.entity.DeliveryPoint;
import com.badier.badier_ride.entity.Dispatcher;
import com.badier.badier_ride.entity.Driver;
import com.badier.badier_ride.entity.Route;
import com.badier.badier_ride.enumeration.RouteStatus;
import com.badier.badier_ride.repository.DeliveryPointRepository;
import com.badier.badier_ride.repository.DispatcherRepository;
import com.badier.badier_ride.repository.DriverRepository;
import com.badier.badier_ride.repository.RouteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final DriverRepository driverRepository;
    private final DispatcherRepository dispatcherRepository;
    private final DeliveryPointRepository deliveryPointRepository;
    private final DeliveryPointService deliveryPointService;

    @Transactional
    public RouteResponse createRoute(RouteRequest request) {
        // Récupérer le chauffeur
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé avec ID: " + request.getDriverId()));

        // Récupérer le répartiteur
        Dispatcher dispatcher = dispatcherRepository.findById(request.getDispatcherId())
                .orElseThrow(() -> new RuntimeException("Répartiteur non trouvé avec ID: " + request.getDispatcherId()));

        // Récupérer les points de livraison
        List<DeliveryPoint> deliveryPoints = new ArrayList<>();
        if (request.getDeliveryPointIds() != null && !request.getDeliveryPointIds().isEmpty()) {
            deliveryPoints = deliveryPointRepository.findAllById(request.getDeliveryPointIds());
            if (deliveryPoints.size() != request.getDeliveryPointIds().size()) {
                throw new RuntimeException("Certains points de livraison n'ont pas été trouvés");
            }
        }

        // Créer la tournée
        Route route = Route.builder()
                .name(request.getName())
                .driver(driver)
                .dispatcher(dispatcher)
                .deliveryPoints(deliveryPoints)
                .status(RouteStatus.valueOf(request.getStatus()))
                .startTime(LocalDateTime.parse(request.getStartTime()))
                .endTime(LocalDateTime.parse(request.getEndTime()))
                .notes(request.getNotes())
                .build();

        Route savedRoute = routeRepository.save(route);
        return mapToResponse(savedRoute);
    }

    public RouteResponse getRouteById(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournée non trouvée avec ID: " + id));
        return mapToResponse(route);
    }

    public List<RouteResponse> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RouteResponse> getRoutesByStatus(String status) {
        return routeRepository.findByStatus(RouteStatus.valueOf(status)).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RouteResponse> getRoutesByDriver(Long driverId) {
        return routeRepository.findByDriverId(driverId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RouteResponse> getRoutesByDispatcher(Long dispatcherId) {
        return routeRepository.findByDispatcherId(dispatcherId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RouteResponse updateRoute(Long id, RouteRequest request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournée non trouvée avec ID: " + id));

        // Mise à jour du chauffeur si nécessaire
        if (request.getDriverId() != null && !request.getDriverId().equals(route.getDriver().getId())) {
            Driver driver = driverRepository.findById(request.getDriverId())
                    .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé avec ID: " + request.getDriverId()));
            route.setDriver(driver);
        }

        // Mise à jour du répartiteur si nécessaire
        if (request.getDispatcherId() != null && !request.getDispatcherId().equals(route.getDispatcher().getId())) {
            Dispatcher dispatcher = dispatcherRepository.findById(request.getDispatcherId())
                    .orElseThrow(() -> new RuntimeException("Répartiteur non trouvé avec ID: " + request.getDispatcherId()));
            route.setDispatcher(dispatcher);
        }

        // Mise à jour des points de livraison si nécessaire
        if (request.getDeliveryPointIds() != null) {
            List<DeliveryPoint> deliveryPoints = deliveryPointRepository.findAllById(request.getDeliveryPointIds());
            if (deliveryPoints.size() != request.getDeliveryPointIds().size()) {
                throw new RuntimeException("Certains points de livraison n'ont pas été trouvés");
            }
            route.setDeliveryPoints(deliveryPoints);
        }

        // Mise à jour des autres champs
        if (request.getName() != null) route.setName(request.getName());
        if (request.getStatus() != null) route.setStatus(RouteStatus.valueOf(request.getStatus()));
        if (request.getStartTime() != null) route.setStartTime(LocalDateTime.parse(request.getStartTime()));
        if (request.getEndTime() != null) route.setEndTime(LocalDateTime.parse(request.getEndTime()));
        if (request.getNotes() != null) route.setNotes(request.getNotes());

        Route updatedRoute = routeRepository.save(route);
        return mapToResponse(updatedRoute);
    }

    @Transactional
    public RouteResponse updateRouteStatus(Long id, String status) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournée non trouvée avec ID: " + id));

        RouteStatus routeStatus = RouteStatus.valueOf(status);
        route.setStatus(routeStatus);

        // Si la tournée passe à IN_PROGRESS et qu'elle n'a pas de startTime défini
        if (routeStatus == RouteStatus.IN_PROGRESS && route.getStartTime() == null) {
            route.setStartTime(LocalDateTime.now());
        }

        // Si la tournée passe à COMPLETED et qu'elle n'a pas de endTime défini
        if (routeStatus == RouteStatus.COMPLETED && route.getEndTime() == null) {
            route.setEndTime(LocalDateTime.now());
        }

        Route updatedRoute = routeRepository.save(route);
        return mapToResponse(updatedRoute);
    }

    @Transactional
    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
    }

    // Méthode pour ajouter un point de livraison à une tournée
    @Transactional
    public RouteResponse addDeliveryPointToRoute(Long routeId, Long deliveryPointId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Tournée non trouvée avec ID: " + routeId));

        DeliveryPoint deliveryPoint = deliveryPointRepository.findById(deliveryPointId)
                .orElseThrow(() -> new RuntimeException("Point de livraison non trouvé avec ID: " + deliveryPointId));

        if (!route.getDeliveryPoints().contains(deliveryPoint)) {
            route.getDeliveryPoints().add(deliveryPoint);
            routeRepository.save(route);
        }

        return mapToResponse(route);
    }

    // Méthode pour retirer un point de livraison d'une tournée
    @Transactional
    public RouteResponse removeDeliveryPointFromRoute(Long routeId, Long deliveryPointId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Tournée non trouvée avec ID: " + routeId));

        DeliveryPoint deliveryPoint = deliveryPointRepository.findById(deliveryPointId)
                .orElseThrow(() -> new RuntimeException("Point de livraison non trouvé avec ID: " + deliveryPointId));

        route.getDeliveryPoints().remove(deliveryPoint);
        routeRepository.save(route);

        return mapToResponse(route);
    }

    // Méthode pour mapper l'entité vers DTO
    private RouteResponse mapToResponse(Route route) {
        return RouteResponse.builder()
                .id(route.getId())
                .name(route.getName())
                .driver(mapDriverToSummary(route.getDriver()))
                .dispatcher(mapDispatcherToSummary(route.getDispatcher()))
                .deliveryPoints(route.getDeliveryPoints().stream()
                        .map(deliveryPointService::mapToResponse)
                        .collect(Collectors.toList()))
                .status(route.getStatus().toString())
                .startTime(route.getStartTime() != null ? route.getStartTime().toString() : null)
                .endTime(route.getEndTime() != null ? route.getEndTime().toString() : null)
                .notes(route.getNotes())
                .build();
    }

    private UserSummaryResponse mapDriverToSummary(Driver driver) {
        return UserSummaryResponse.builder()
                .id(driver.getId())
                .username(driver.getUsername())
                .email(driver.getEmail())
                .build();
    }

    private UserSummaryResponse mapDispatcherToSummary(Dispatcher dispatcher) {
        return UserSummaryResponse.builder()
                .id(dispatcher.getId())
                .username(dispatcher.getUsername())
                .email(dispatcher.getEmail())
                .build();
    }
}