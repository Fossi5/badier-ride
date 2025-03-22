package com.badier.badier_ride.service;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.badier.badier_ride.entity.DeliveryPoint;
import com.badier.badier_ride.entity.Location;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapsApiService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${maps.api.key}")
    private String apiKey;
    
    @Value("${maps.api.url:https://maps.googleapis.com/maps/api}")
    private String apiBaseUrl;
    
    /**
     * Calcule la distance et le temps de trajet entre deux points
     * @return Map contenant la distance (en mètres) et la durée (en secondes)
     */
    public Map<String, Double> getDistanceAndDuration(
            double originLat, double originLng, 
            double destLat, double destLng) {
        
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(apiBaseUrl + "/distancematrix/json")
                    .queryParam("origins", originLat + "," + originLng)
                    .queryParam("destinations", destLat + "," + destLng)
                    .queryParam("mode", "driving")
                    .queryParam("key", apiKey)
                    .build()
                    .toUri();
            
            log.debug("Appel API Google Maps: {}", uri);
            
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            
            if (!"OK".equals(root.path("status").asText())) {
                log.error("Erreur lors de l'appel à l'API Google Maps: {}", root.path("status").asText());
                throw new RuntimeException("Erreur lors de l'appel à l'API Google Maps: " + root.path("status").asText());
            }
            
            JsonNode element = root.path("rows").get(0).path("elements").get(0);
            
            if (!"OK".equals(element.path("status").asText())) {
                log.warn("Pas de route trouvée entre les points: {}", element.path("status").asText());
                return Map.of("distance", 0.0, "duration", 0.0);
            }
            
            double distance = element.path("distance").path("value").asDouble();
            double duration = element.path("duration").path("value").asDouble();
            
            return Map.of("distance", distance, "duration", duration);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'appel à l'API Google Maps", e);
            // En cas d'erreur, retourner un fallback
            return Map.of("distance", 0.0, "duration", 0.0);
        }
    }
    
    /**
     * Calcule le meilleur itinéraire pour visiter une série de points
     * @return Liste d'indices des points dans l'ordre optimal selon Google Maps
     */
    public List<Integer> getOptimalWaypoints(
            Location origin, 
            List<DeliveryPoint> deliveryPoints) {
        
        try {
            // Construire la liste des waypoints
            StringBuilder waypointsBuilder = new StringBuilder("optimize:true|");
            for (int i = 0; i < deliveryPoints.size(); i++) {
                DeliveryPoint point = deliveryPoints.get(i);
                if (i > 0) waypointsBuilder.append("|");
                waypointsBuilder.append(point.getAddress().getLatitude())
                               .append(",")
                               .append(point.getAddress().getLongitude());
            }
            
            // Construire l'URI pour l'API Directions
            URI uri = UriComponentsBuilder.fromHttpUrl(apiBaseUrl + "/directions/json")
                    .queryParam("origin", origin.getLatitude() + "," + origin.getLongitude())
                    .queryParam("destination", origin.getLatitude() + "," + origin.getLongitude()) // Retour au point de départ
                    .queryParam("waypoints", waypointsBuilder.toString())
                    .queryParam("mode", "driving")
                    .queryParam("key", apiKey)
                    .build()
                    .toUri();
            
            log.debug("Appel API Google Maps Directions: {}", uri);
            
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            
            if (!"OK".equals(root.path("status").asText())) {
                log.error("Erreur lors de l'appel à l'API Google Maps Directions: {}", root.path("status").asText());
                throw new RuntimeException("Erreur lors de l'appel à l'API Google Maps Directions");
            }
            
            // Récupérer l'ordre optimisé des waypoints
            JsonNode waypointOrder = root.path("routes").get(0).path("waypoint_order");
            
            // Construire la liste des indices dans le nouvel ordre
            List<Integer> optimizedOrder = new java.util.ArrayList<>();
            for (JsonNode index : waypointOrder) {
                optimizedOrder.add(index.asInt());
            }
            
            return optimizedOrder;
            
        } catch (Exception e) {
            log.error("Erreur lors de l'appel à l'API Google Maps Directions", e);
            // En cas d'erreur, retourner l'ordre original
            List<Integer> defaultOrder = new java.util.ArrayList<>();
            for (int i = 0; i < deliveryPoints.size(); i++) {
                defaultOrder.add(i);
            }
            return defaultOrder;
        }
    }
    
    /**
     * Méthode de fallback pour calculer la distance à vol d'oiseau
     * @return Distance en kilomètres
     */
    public double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        // Rayon de la Terre en km
        final double R = 6371.0;
        
        // Conversion en radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        
        // Formule de Haversine
        double dlon = lon2Rad - lon1Rad;
        double dlat = lat2Rad - lat1Rad;
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // Distance en kilomètres
        return R * c;
    }
}