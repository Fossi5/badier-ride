package com.badier.badier_ride.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.badier.badier_ride.dto.RouteResponse;
import com.badier.badier_ride.entity.DeliveryPoint;
import com.badier.badier_ride.entity.Driver;
import com.badier.badier_ride.entity.Location;
import com.badier.badier_ride.entity.Route;
import com.badier.badier_ride.repository.DriverRepository;
import com.badier.badier_ride.repository.RouteRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RouteOptimizationService {

    @Autowired
    private RouteRepository routeRepository;
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Autowired
    private MapsApiService mapsApiService;
    
    /**
     * Optimise l'ordre des points de livraison pour une tournée donnée
     */
    @Transactional
    public Route optimizeRoute(Long routeId) {
        log.info("Optimisation de la tournée avec ID: {}", routeId);
        
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Tournée non trouvée avec ID: " + routeId));
        
        // Récupérer la position de départ (position actuelle du chauffeur)
        Driver driver = driverRepository.findById(route.getDriver().getId())
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé pour la tournée: " + route.getId()));
        
        Location startLocation = driver.getCurrentLocation();
        if (startLocation == null) {
            log.warn("Position du chauffeur non disponible, utilisation d'une position par défaut");
            // Utiliser une position par défaut
            startLocation = new Location();
            startLocation.setLatitude(48.8566); // Paris par exemple
            startLocation.setLongitude(2.3522);
        }
        
        // Traiter les points prioritaires (points 1, 2, 3)
        List<DeliveryPoint> allPoints = new ArrayList<>(route.getDeliveryPoints());
        List<DeliveryPoint> priorityPoints = extractPriorityPoints(allPoints);
        
        // Liste finale des points optimisés
        List<DeliveryPoint> optimizedPoints = new ArrayList<>();
        
        // Ajouter d'abord les points prioritaires
        if (!priorityPoints.isEmpty()) {
            log.info("{} points prioritaires trouvés, ils seront traités en premier", priorityPoints.size());
            optimizedPoints.addAll(priorityPoints);
            allPoints.removeAll(priorityPoints);
        }
        
        // Si des points restent à optimiser, utiliser l'API Google Maps ou l'algorithme de fallback
        if (!allPoints.isEmpty()) {
            try {
                log.info("Optimisation des {} points restants avec Google Maps", allPoints.size());
                optimizedPoints.addAll(optimizeWithGoogleMaps(startLocation, allPoints));
            } catch (Exception e) {
                log.error("Erreur lors de l'appel à l'API Google Maps, utilisation de l'algorithme de fallback", e);
                // En cas d'erreur, utiliser l'algorithme de fallback (plus proche voisin)
                optimizedPoints.addAll(findOptimalOrderFallback(startLocation, allPoints));
            }
        }
        
        route.setDeliveryPoints(optimizedPoints);
        Route savedRoute = routeRepository.save(route);
        log.info("Tournée {} optimisée avec succès. Nombre de points: {}", routeId, optimizedPoints.size());
        
        return savedRoute;
    }
    
    /**
     * Optimisation avec Google Maps Directions API
     */
    private List<DeliveryPoint> optimizeWithGoogleMaps(Location startLocation, List<DeliveryPoint> points) {
        // Obtenir l'ordre optimisé des waypoints via l'API Google Maps
        List<Integer> optimizedOrder = mapsApiService.getOptimalWaypoints(startLocation, points);
        
        // Réorganiser les points selon l'ordre retourné par Google Maps
        List<DeliveryPoint> result = new ArrayList<>();
        for (Integer index : optimizedOrder) {
            result.add(points.get(index));
        }
        
        return result;
    }
    
    /**
     * Algorithme du plus proche voisin comme fallback en cas d'échec de l'API
     */
    private List<DeliveryPoint> findOptimalOrderFallback(Location startLocation, List<DeliveryPoint> points) {
        List<DeliveryPoint> allPoints = new ArrayList<>(points);
        List<DeliveryPoint> optimizedRoute = new ArrayList<>();
        
        if (allPoints.isEmpty()) {
            return optimizedRoute;
        }
        
        // Trouver le premier point (le plus proche du départ)
        DeliveryPoint currentPoint = findNearestPoint(startLocation.getLatitude(), startLocation.getLongitude(), allPoints);
        optimizedRoute.add(currentPoint);
        allPoints.remove(currentPoint);
        
        // Algorithme du plus proche voisin
        while (!allPoints.isEmpty()) {
            DeliveryPoint nextPoint = findNearestPoint(
                currentPoint.getAddress().getLatitude(),
                currentPoint.getAddress().getLongitude(),
                allPoints
            );
            
            optimizedRoute.add(nextPoint);
            allPoints.remove(nextPoint);
            currentPoint = nextPoint;
        }
        
        return optimizedRoute;
    }
    
    /**
     * Trouve le point le plus proche des coordonnées données
     */
    private DeliveryPoint findNearestPoint(Double latitude, Double longitude, List<DeliveryPoint> points) {
        DeliveryPoint nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (DeliveryPoint point : points) {
            // S'assurer que les coordonnées sont disponibles
            if (point.getAddress() == null || 
                point.getAddress().getLatitude() == null || 
                point.getAddress().getLongitude() == null) {
                log.warn("Point de livraison {} sans coordonnées valides, ignoré", point.getId());
                continue;
            }
            
            // Essayer d'utiliser l'API Google Maps pour la distance réelle
            try {
                Map<String, Double> distanceInfo = mapsApiService.getDistanceAndDuration(
                    latitude, longitude,
                    point.getAddress().getLatitude(),
                    point.getAddress().getLongitude()
                );
                
                double distance = distanceInfo.get("distance");
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = point;
                }
            } catch (Exception e) {
                log.warn("Impossible d'obtenir la distance via Google Maps, utilisation de la distance à vol d'oiseau", e);
                // Fallback : calcul à vol d'oiseau
                double distance = mapsApiService.calculateHaversineDistance(
                    latitude, longitude,
                    point.getAddress().getLatitude(),
                    point.getAddress().getLongitude()
                ) * 1000; // Convertir km en mètres
                
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = point;
                }
            }
        }
        
        return nearest;
    }
    
    /**
     * Extrait les points prioritaires dans un ordre prédéfini
     */
    private List<DeliveryPoint> extractPriorityPoints(List<DeliveryPoint> allPoints) {
        List<DeliveryPoint> priorityPoints = new ArrayList<>();
        
        // Identifiants des points prioritaires dans l'ordre voulu
        List<Long> priorityIds = List.of(1L, 2L, 3L);
        
        // Extraire les points dans l'ordre spécifié
        for (Long priorityId : priorityIds) {
            for (DeliveryPoint point : allPoints) {
                if (point.getId().equals(priorityId)) {
                    priorityPoints.add(point);
                    break;
                }
            }
        }
        
        return priorityPoints;
    }
    
    /**
     * Calcule la distance totale d'une tournée
     */
    public double calculateTotalRouteDistance(Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Tournée non trouvée avec ID: " + routeId));
        
        List<DeliveryPoint> points = route.getDeliveryPoints();
        if (points == null || points.isEmpty()) {
            return 0.0;
        }
        
        double totalDistance = 0.0;
        Location currentLocation = null;
        
        // Obtenir la position de départ (position actuelle du chauffeur)
        Driver driver = driverRepository.findById(route.getDriver().getId())
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé pour la tournée: " + route.getId()));
        
        currentLocation = driver.getCurrentLocation();
        
        // Si la position du chauffeur n'est pas disponible, partir du premier point
        if (currentLocation == null && !points.isEmpty()) {
            DeliveryPoint firstPoint = points.get(0);
            currentLocation = new Location();
            currentLocation.setLatitude(firstPoint.getAddress().getLatitude());
            currentLocation.setLongitude(firstPoint.getAddress().getLongitude());
            points = points.subList(1, points.size()); // Ne pas compter le premier point
        }
        
        // Calculer la distance pour chaque segment
        for (DeliveryPoint point : points) {
            if (currentLocation != null) {
                try {
                    Map<String, Double> distanceInfo = mapsApiService.getDistanceAndDuration(
                        currentLocation.getLatitude(), currentLocation.getLongitude(),
                        point.getAddress().getLatitude(), point.getAddress().getLongitude()
                    );
                    totalDistance += distanceInfo.get("distance");
                } catch (Exception e) {
                    // Fallback: utiliser la distance à vol d'oiseau
                    double distanceInMeters = mapsApiService.calculateHaversineDistance(
                        currentLocation.getLatitude(), currentLocation.getLongitude(),
                        point.getAddress().getLatitude(), point.getAddress().getLongitude()
                    ) * 1000; // convertir km en mètres
                    totalDistance += distanceInMeters;
                }
            }
            
            // Mettre à jour la position courante
            currentLocation = new Location();
            currentLocation.setLatitude(point.getAddress().getLatitude());
            currentLocation.setLongitude(point.getAddress().getLongitude());
        }
        
        // Retourner la distance en kilomètres
        return totalDistance / 1000.0;
    }
}