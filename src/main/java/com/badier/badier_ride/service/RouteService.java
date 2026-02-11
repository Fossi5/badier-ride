package com.badier.badier_ride.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.badier.badier_ride.dto.DeliveryPointOrderDto;
import com.badier.badier_ride.dto.RouteRequest;
import com.badier.badier_ride.dto.RouteResponse;
import com.badier.badier_ride.dto.DeliveryPointResponse;
import com.badier.badier_ride.dto.UserSummaryResponse;
import com.badier.badier_ride.entity.DeliveryPoint;
import com.badier.badier_ride.entity.Route;
import com.badier.badier_ride.entity.RouteDeliveryPoint;
import com.badier.badier_ride.entity.User;
import com.badier.badier_ride.enumeration.RouteStatus;
import com.badier.badier_ride.repository.DeliveryPointRepository;
import com.badier.badier_ride.repository.RouteDeliveryPointRepository;
import com.badier.badier_ride.repository.RouteRepository;
import com.badier.badier_ride.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RouteService {

    private static final Logger log = LoggerFactory.getLogger(RouteService.class);

    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final DeliveryPointRepository deliveryPointRepository;
    private final DeliveryPointService deliveryPointService;
    private final RouteDeliveryPointRepository routeDeliveryPointRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Transactional
    public RouteResponse createRoute(RouteRequest request) {
        log.info("Creating a new route: {}", request.getName());

        User driver = userRepository.findById(request.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + request.getDriverId()));

        User dispatcher = userRepository.findById(request.getDispatcherId())
                .orElseThrow(() -> new RuntimeException("Dispatcher not found with ID: " + request.getDispatcherId()));

        List<DeliveryPoint> deliveryPoints = new ArrayList<>();
        if (request.getDeliveryPointIds() != null && !request.getDeliveryPointIds().isEmpty()) {
            deliveryPoints = deliveryPointRepository.findAllById(request.getDeliveryPointIds());
            if (deliveryPoints.size() != request.getDeliveryPointIds().size()) {
                throw new RuntimeException("Some delivery points were not found");
            }
        }

        Route route = Route.builder()
                .name(request.getName())
                .driver(driver)
                .dispatcher(dispatcher)
                .deliveryPoints(deliveryPoints)
                .status(RouteStatus.valueOf(request.getStatus()))
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .notes(request.getNotes())
                .build();

        Route savedRoute = routeRepository.save(route);

        // Créer les entrées RouteDeliveryPoint avec le statut initialisé à PENDING
        if (!deliveryPoints.isEmpty()) {
            int sequenceOrder = 0;
            for (DeliveryPoint point : deliveryPoints) {
                RouteDeliveryPoint rdp = RouteDeliveryPoint.builder()
                        .route(savedRoute)
                        .deliveryPoint(point)
                        .sequenceOrder(sequenceOrder++)
                        .status(com.badier.badier_ride.enumeration.DeliveryStatus.PENDING)
                        .isStartPoint(false)
                        .isEndPoint(false)
                        .plannedTime(point.getPlannedTime())
                        .build();
                routeDeliveryPointRepository.save(rdp);
            }
        }

        log.info("Route created successfully with ID: {}", savedRoute.getId());
        return mapToResponse(savedRoute);
    }

    public RouteResponse getRouteById(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found with ID: " + id));
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

    public List<RouteResponse> getRoutesByDriverUsername(String username) {
        User driver = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return getRoutesByDriver(driver.getId());
    }

    public List<RouteResponse> getRoutesByDispatcher(Long dispatcherId) {
        return routeRepository.findByDispatcherId(dispatcherId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RouteResponse updateRoute(Long id, RouteRequest request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found with ID: " + id));

        if (request.getDriverId() != null && !request.getDriverId().equals(route.getDriver().getId())) {
            User driver = userRepository.findById(request.getDriverId())
                    .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + request.getDriverId()));
            route.setDriver(driver);
        }

        if (request.getDispatcherId() != null && !request.getDispatcherId().equals(route.getDispatcher().getId())) {
            User dispatcher = userRepository.findById(request.getDispatcherId())
                    .orElseThrow(
                            () -> new RuntimeException("Dispatcher not found with ID: " + request.getDispatcherId()));
            route.setDispatcher(dispatcher);
        }

        if (request.getDeliveryPointIds() != null) {
            List<DeliveryPoint> deliveryPoints = deliveryPointRepository.findAllById(request.getDeliveryPointIds());
            if (deliveryPoints.size() != request.getDeliveryPointIds().size()) {
                throw new RuntimeException("Some delivery points were not found");
            }
            route.setDeliveryPoints(deliveryPoints);
        }

        if (request.getName() != null)
            route.setName(request.getName());
        if (request.getStatus() != null)
            route.setStatus(RouteStatus.valueOf(request.getStatus()));
        if (request.getStartTime() != null)
            route.setStartTime(request.getStartTime());
        if (request.getEndTime() != null)
            route.setEndTime(request.getEndTime());
        if (request.getNotes() != null)
            route.setNotes(request.getNotes());

        Route updatedRoute = routeRepository.save(route);
        log.info("Route updated successfully with ID: {}", updatedRoute.getId());
        return mapToResponse(updatedRoute);
    }

    @Transactional
    public RouteResponse updateRoutePointsOrder(Long routeId, List<DeliveryPointOrderDto> orderedPoints) {
        log.info("Mise à jour de l'ordre des points pour la tournée {}", routeId);

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Tournée non trouvée avec ID: " + routeId));

        // Vider les relations existantes
        routeDeliveryPointRepository.deleteByRouteId(routeId);

        // Créer les nouvelles relations avec l'ordre spécifié
        List<RouteDeliveryPoint> newRelations = new ArrayList<>();

        for (DeliveryPointOrderDto pointOrder : orderedPoints) {
            DeliveryPoint deliveryPoint = deliveryPointRepository.findById(pointOrder.getId())
                    .orElseThrow(
                            () -> new RuntimeException("Point de livraison non trouvé avec ID: " + pointOrder.getId()));

            // Vérifier que le point appartient à la tournée
            if (!route.getDeliveryPoints().contains(deliveryPoint)) {
                throw new RuntimeException(
                        "Le point " + pointOrder.getId() + " n'appartient pas à la tournée " + routeId);
            }

            RouteDeliveryPoint rdp = RouteDeliveryPoint.builder()
                    .route(route)
                    .deliveryPoint(deliveryPoint)
                    .sequenceOrder(pointOrder.getSequenceOrder())
                    .isStartPoint(pointOrder.getIsStartPoint())
                    .isEndPoint(pointOrder.getIsEndPoint())
                    .build();

            newRelations.add(rdp);
        }

        // Sauvegarder les nouvelles relations
        routeDeliveryPointRepository.saveAll(newRelations);

        // Recharger la tournée pour obtenir les relations mises à jour
        Route updatedRoute = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Tournée non trouvée avec ID: " + routeId));

        log.info("Ordre des points mis à jour avec succès pour la tournée {}", routeId);
        return mapToResponse(updatedRoute);
    }

    @Transactional
    public RouteResponse updateRouteStatus(Long id, String status) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found with ID: " + id));

        RouteStatus routeStatus = RouteStatus.valueOf(status);

        // Validation critique : Empêcher la clôture de la tournée si tous les points ne
        // sont pas traités
        if (routeStatus == RouteStatus.COMPLETED) {
            // Récupérer les RouteDeliveryPoint (avec le statut spécifique à cette tournée)
            List<RouteDeliveryPoint> routeDeliveryPoints = routeDeliveryPointRepository
                    .findByRouteIdOrderBySequenceOrderAsc(id);

            // Filtrer les points non traités (ni COMPLETED ni FAILED)
            List<RouteDeliveryPoint> untreatedPoints = routeDeliveryPoints.stream()
                    .filter(rdp -> rdp.getStatus() != com.badier.badier_ride.enumeration.DeliveryStatus.COMPLETED
                            && rdp.getStatus() != com.badier.badier_ride.enumeration.DeliveryStatus.FAILED)
                    .collect(Collectors.toList());

            if (!untreatedPoints.isEmpty()) {
                String untreatedPointsInfo = untreatedPoints.stream()
                        .map(rdp -> String.format("Point #%d (%s) - Statut: %s",
                                rdp.getDeliveryPoint().getId(),
                                rdp.getDeliveryPoint().getClientName(),
                                rdp.getStatus()))
                        .collect(Collectors.joining(", "));

                log.warn("Tentative de clôture de la tournée {} avec {} points non traités: {}",
                        id, untreatedPoints.size(), untreatedPointsInfo);

                throw new IllegalStateException(
                        String.format(
                                "Impossible de terminer la tournée. %d point(s) de livraison n'ont pas été traité(s). "
                                        +
                                        "Vous devez marquer chaque point comme 'Livré' ou 'Échec' avant de terminer la tournée.",
                                untreatedPoints.size()));
            }
            log.info("Tous les {} points de livraison ont été traités. Clôture de la tournée autorisée.",
                    routeDeliveryPoints.size());
        }

        route.setStatus(routeStatus);

        if (routeStatus == RouteStatus.IN_PROGRESS && route.getStartTime() == null) {
            route.setStartTime(LocalDateTime.now());
        }

        if (routeStatus == RouteStatus.COMPLETED && route.getEndTime() == null) {
            route.setEndTime(LocalDateTime.now());
        }

        Route updatedRoute = routeRepository.save(route);
        log.info("Route status updated successfully with ID: {} to status: {}", updatedRoute.getId(), routeStatus);
        return mapToResponse(updatedRoute);
    }

    @Transactional
    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
        log.info("Route deleted successfully with ID: {}", id);
    }

    @Transactional
    public RouteResponse addDeliveryPointToRoute(Long routeId, Long deliveryPointId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found with ID: " + routeId));

        DeliveryPoint deliveryPoint = deliveryPointRepository.findById(deliveryPointId)
                .orElseThrow(() -> new RuntimeException("Delivery point not found with ID: " + deliveryPointId));

        if (!route.getDeliveryPoints().contains(deliveryPoint)) {
            route.getDeliveryPoints().add(deliveryPoint);
            routeRepository.save(route);
        }

        log.info("Delivery point added to route with ID: {}", routeId);
        return mapToResponse(route);
    }

    @Transactional
    public RouteResponse removeDeliveryPointFromRoute(Long routeId, Long deliveryPointId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found with ID: " + routeId));

        DeliveryPoint deliveryPoint = deliveryPointRepository.findById(deliveryPointId)
                .orElseThrow(() -> new RuntimeException("Delivery point not found with ID: " + deliveryPointId));

        route.getDeliveryPoints().remove(deliveryPoint);
        routeRepository.save(route);

        log.info("Delivery point removed from route with ID: {}", routeId);
        return mapToResponse(route);
    }

    public RouteResponse mapToResponse(Route route) {
        List<RouteDeliveryPoint> routeDeliveryPoints = routeDeliveryPointRepository
                .findByRouteIdOrderBySequenceOrderAsc(route.getId());

        List<DeliveryPointResponse> deliveryPointResponses;

        if (!routeDeliveryPoints.isEmpty()) {
            Map<Long, RouteDeliveryPoint> uniquePoints = new LinkedHashMap<>();

            for (RouteDeliveryPoint routeDeliveryPoint : routeDeliveryPoints) {
                if (routeDeliveryPoint.getDeliveryPoint() == null) {
                    continue;
                }

                Long deliveryPointId = routeDeliveryPoint.getDeliveryPoint().getId();
                RouteDeliveryPoint existing = uniquePoints.get(deliveryPointId);

                if (existing == null) {
                    uniquePoints.put(deliveryPointId, routeDeliveryPoint);
                    continue;
                }

                boolean existingHasOrder = existing.getSequenceOrder() != null;
                boolean candidateHasOrder = routeDeliveryPoint.getSequenceOrder() != null;

                if (!existingHasOrder && candidateHasOrder) {
                    log.warn("Duplicate delivery point {} detected on route {}. Prioritizing the ordered instance.",
                            deliveryPointId, route.getId());
                    uniquePoints.put(deliveryPointId, routeDeliveryPoint);
                }
            }

            deliveryPointResponses = uniquePoints.values().stream()
                    .sorted(Comparator.comparing(rdp -> Optional.ofNullable(rdp.getSequenceOrder())
                            .orElse(Integer.MAX_VALUE)))
                    .map(this::mapRouteDeliveryPointToResponse)
                    .collect(Collectors.toList());
        } else if (route.getDeliveryPoints() != null) {
            deliveryPointResponses = route.getDeliveryPoints().stream()
                    .map(deliveryPointService::mapToResponse)
                    .collect(Collectors.toList());
        } else {
            deliveryPointResponses = new ArrayList<>();
        }

        return RouteResponse.builder()
                .id(route.getId())
                .name(route.getName())
                .driver(mapUserToSummary(route.getDriver()))
                .dispatcher(mapUserToSummary(route.getDispatcher()))
                .deliveryPoints(deliveryPointResponses)
                .status(route.getStatus())
                .startTime(route.getStartTime())
                .endTime(route.getEndTime())
                .notes(route.getNotes())
                .build();
    }

    private DeliveryPointResponse mapRouteDeliveryPointToResponse(RouteDeliveryPoint routeDeliveryPoint) {
        DeliveryPointResponse response = deliveryPointService.mapToResponse(routeDeliveryPoint.getDeliveryPoint());

        if (routeDeliveryPoint.getStatus() != null) {
            response.setDeliveryStatus(routeDeliveryPoint.getStatus().name());
        }

        response.setSequenceOrder(routeDeliveryPoint.getSequenceOrder());
        response.setIsStartPoint(Boolean.TRUE.equals(routeDeliveryPoint.getIsStartPoint()));
        response.setIsEndPoint(Boolean.TRUE.equals(routeDeliveryPoint.getIsEndPoint()));

        if (routeDeliveryPoint.getPlannedTime() != null) {
            response.setPlannedTime(routeDeliveryPoint.getPlannedTime());
            response.setDeliveryTime(routeDeliveryPoint.getPlannedTime().toString());
            response.setDeliveryDate(routeDeliveryPoint.getPlannedTime().toString());
        }

        if (routeDeliveryPoint.getActualTime() != null) {
            response.setActualTime(routeDeliveryPoint.getActualTime());
        }

        return response;
    }

    private UserSummaryResponse mapUserToSummary(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

}