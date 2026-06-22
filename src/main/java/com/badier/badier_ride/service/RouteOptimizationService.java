package com.badier.badier_ride.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.badier.badier_ride.entity.DeliveryPoint;
import com.badier.badier_ride.entity.Driver;
import com.badier.badier_ride.entity.Location;
import com.badier.badier_ride.entity.Route;
import com.badier.badier_ride.entity.RouteDeliveryPoint;
import com.badier.badier_ride.enumeration.DeliveryStatus;
import com.badier.badier_ride.repository.DriverRepository;
import com.badier.badier_ride.repository.RouteDeliveryPointRepository;
import com.badier.badier_ride.repository.RouteRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RouteOptimizationService {

    @Value("${app.defaults.latitude:48.8566}")
    private double defaultLatitude;

    @Value("${app.defaults.longitude:2.3522}")
    private double defaultLongitude;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private MapsApiService mapsApiService;

    @Autowired
    private RouteDeliveryPointRepository routeDeliveryPointRepository;

    @Transactional
    public Route optimizeRoute(Long routeId) {
        log.info("Optimisation de la tournée avec ID: {}", routeId);

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Tournée non trouvée avec ID: " + routeId));

        Driver driver = driverRepository.findById(route.getDriver().getId())
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé pour la tournée: " + route.getId()));

        Location startLocation = driver.getCurrentLocation();
        if (startLocation == null) {
            log.warn("Position du chauffeur non disponible, utilisation d'une position par défaut");
            startLocation = new Location();
            startLocation.setLatitude(defaultLatitude);
            startLocation.setLongitude(defaultLongitude);
        }

        List<RouteDeliveryPoint> associations = routeDeliveryPointRepository
                .findByRouteIdOrderBySequenceOrderAsc(routeId);
        List<DeliveryPoint> allPoints = associations.stream()
                .map(RouteDeliveryPoint::getDeliveryPoint)
                .collect(Collectors.toList());

        List<DeliveryPoint> optimizedPoints;
        try {
            log.info("Optimisation des {} points avec Google Maps", allPoints.size());
            optimizedPoints = optimizeWithGoogleMaps(startLocation, allPoints);
        } catch (Exception e) {
            log.error("Erreur lors de l'appel à l'API Google Maps, utilisation de l'algorithme de fallback", e);
            optimizedPoints = findOptimalOrderFallback(startLocation, allPoints);
        }

        overwriteRouteDeliveryPoints(route, optimizedPoints);
        Route savedRoute = routeRepository.save(route);
        log.info("Tournée {} optimisée avec succès. Nombre de points: {}", routeId, optimizedPoints.size());

        return savedRoute;
    }

    private List<DeliveryPoint> optimizeWithGoogleMaps(Location startLocation, List<DeliveryPoint> points) {
        List<Integer> optimizedOrder = mapsApiService.getOptimalWaypoints(startLocation, points);

        List<DeliveryPoint> result = new ArrayList<>();
        for (Integer index : optimizedOrder) {
            result.add(points.get(index));
        }

        return result;
    }

    private List<DeliveryPoint> findOptimalOrderFallback(Location startLocation, List<DeliveryPoint> points) {
        List<DeliveryPoint> allPoints = new ArrayList<>(points);
        List<DeliveryPoint> optimizedRoute = new ArrayList<>();

        if (allPoints.isEmpty()) {
            return optimizedRoute;
        }

        DeliveryPoint currentPoint = findNearestPoint(startLocation.getLatitude(), startLocation.getLongitude(),
                allPoints);
        optimizedRoute.add(currentPoint);
        allPoints.remove(currentPoint);

        while (!allPoints.isEmpty()) {
            DeliveryPoint nextPoint = findNearestPoint(
                    currentPoint.getAddress().getLatitude(),
                    currentPoint.getAddress().getLongitude(),
                    allPoints);

            optimizedRoute.add(nextPoint);
            allPoints.remove(nextPoint);
            currentPoint = nextPoint;
        }

        return optimizedRoute;
    }

    private DeliveryPoint findNearestPoint(Double latitude, Double longitude, List<DeliveryPoint> points) {
        DeliveryPoint nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (DeliveryPoint point : points) {
            if (point.getAddress() == null ||
                    point.getAddress().getLatitude() == null ||
                    point.getAddress().getLongitude() == null) {
                log.warn("Point de livraison {} sans coordonnées valides, ignoré", point.getId());
                continue;
            }

            double distance = mapsApiService.calculateHaversineDistance(
                    latitude, longitude,
                    point.getAddress().getLatitude(),
                    point.getAddress().getLongitude());

            if (distance < minDistance) {
                minDistance = distance;
                nearest = point;
            }
        }

        return nearest;
    }

    public double calculateTotalRouteDistance(Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Tournée non trouvée avec ID: " + routeId));

        List<DeliveryPoint> points = routeDeliveryPointRepository
                .findByRouteIdOrderBySequenceOrderAsc(routeId)
                .stream()
                .map(RouteDeliveryPoint::getDeliveryPoint)
                .collect(Collectors.toList());
        if (points == null || points.isEmpty()) {
            return 0.0;
        }

        double totalDistance = 0.0;
        Location currentLocation = null;

        Driver driver = driverRepository.findById(route.getDriver().getId())
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé pour la tournée: " + route.getId()));

        currentLocation = driver.getCurrentLocation();

        if (currentLocation == null && !points.isEmpty()) {
            DeliveryPoint firstPoint = points.get(0);
            currentLocation = new Location();
            currentLocation.setLatitude(firstPoint.getAddress().getLatitude());
            currentLocation.setLongitude(firstPoint.getAddress().getLongitude());
            points = points.subList(1, points.size());
        }

        for (DeliveryPoint point : points) {
            if (currentLocation != null) {
                try {
                    Map<String, Double> distanceInfo = mapsApiService.getDistanceAndDuration(
                            currentLocation.getLatitude(), currentLocation.getLongitude(),
                            point.getAddress().getLatitude(), point.getAddress().getLongitude());
                    totalDistance += distanceInfo.get("distance");
                } catch (Exception e) {
                    double distanceInMeters = mapsApiService.calculateHaversineDistance(
                            currentLocation.getLatitude(), currentLocation.getLongitude(),
                            point.getAddress().getLatitude(), point.getAddress().getLongitude()) * 1000;
                    totalDistance += distanceInMeters;
                }
            }

            currentLocation = new Location();
            currentLocation.setLatitude(point.getAddress().getLatitude());
            currentLocation.setLongitude(point.getAddress().getLongitude());
        }

        return totalDistance / 1000.0;
    }

    @Transactional
    public Route optimizeRouteWithFixedPoints(Long routeId) {
        log.info("Optimisation de la tournée avec ID: {} en respectant les points fixes", routeId);

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Tournée non trouvée avec ID: " + routeId));

        List<RouteDeliveryPoint> allPoints = routeDeliveryPointRepository.findByRouteIdOrderBySequenceOrderAsc(routeId);

        if (allPoints.isEmpty()) {
            log.warn("Aucun point de livraison trouvé pour la tournée {}", routeId);
            return route;
        }

        RouteDeliveryPoint startPoint = allPoints.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsStartPoint()))
                .findFirst()
                .orElse(null);

        RouteDeliveryPoint endPoint = allPoints.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsEndPoint()))
                .findFirst()
                .orElse(null);

        List<DeliveryPoint> pointsToOptimize = allPoints.stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsStartPoint()) && !Boolean.TRUE.equals(p.getIsEndPoint()))
                .map(RouteDeliveryPoint::getDeliveryPoint)
                .collect(Collectors.toList());

        Location startLocation;
        if (startPoint != null) {
            DeliveryPoint definedStart = startPoint.getDeliveryPoint();
            startLocation = new Location();
            startLocation.setLatitude(definedStart.getAddress().getLatitude());
            startLocation.setLongitude(definedStart.getAddress().getLongitude());
        } else {
            Driver driver = driverRepository.findById(route.getDriver().getId())
                    .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé pour la tournée: " + route.getId()));

            startLocation = driver.getCurrentLocation();
            if (startLocation == null) {
                startLocation = new Location();
                startLocation.setLatitude(defaultLatitude);
                startLocation.setLongitude(defaultLongitude);
            }
        }

        List<DeliveryPoint> optimizedMiddlePoints;
        try {
            log.info("Optimisation des {} points intermédiaires avec Google Maps", pointsToOptimize.size());
            optimizedMiddlePoints = optimizeWithGoogleMaps(startLocation, pointsToOptimize);
        } catch (Exception e) {
            log.error("Erreur lors de l'appel à l'API Google Maps, utilisation de l'algorithme de fallback", e);
            optimizedMiddlePoints = findOptimalOrderFallback(startLocation, pointsToOptimize);
        }

        List<DeliveryPoint> finalOrder = new ArrayList<>();

        if (startPoint != null) {
            finalOrder.add(startPoint.getDeliveryPoint());
        }

        finalOrder.addAll(optimizedMiddlePoints);

        if (endPoint != null) {
            finalOrder.add(endPoint.getDeliveryPoint());
        }

        for (int i = 0; i < finalOrder.size(); i++) {
            DeliveryPoint point = finalOrder.get(i);

            RouteDeliveryPoint rdp = allPoints.stream()
                    .filter(p -> p.getDeliveryPoint().getId().equals(point.getId()))
                    .findFirst()
                    .orElse(null);

            if (rdp != null) {
                rdp.setSequenceOrder(i);
            }
        }

        overwriteRouteDeliveryPoints(route, finalOrder);
        return routeRepository.save(route);
    }

    private void overwriteRouteDeliveryPoints(Route route, List<DeliveryPoint> orderedPoints) {
        routeDeliveryPointRepository.deleteByRouteId(route.getId());

        if (orderedPoints == null || orderedPoints.isEmpty()) {
            return;
        }

        int sequence = 0;
        for (DeliveryPoint point : orderedPoints) {
            RouteDeliveryPoint rdp = RouteDeliveryPoint.builder()
                    .route(route)
                    .deliveryPoint(point)
                    .sequenceOrder(sequence++)
                    .status(DeliveryStatus.PENDING)
                    .isStartPoint(false)
                    .isEndPoint(false)
                    .plannedTime(point.getPlannedTime())
                    .build();
            routeDeliveryPointRepository.save(rdp);
        }
    }
}
