package com.badier.badier_ride.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badier.badier_ride.dto.AddressRequest;
import com.badier.badier_ride.dto.AddressResponse;
import com.badier.badier_ride.dto.DeliveryPointRequest;
import com.badier.badier_ride.dto.DeliveryPointResponse;
import com.badier.badier_ride.entity.Address;
import com.badier.badier_ride.entity.DeliveryPoint;
import com.badier.badier_ride.entity.RouteDeliveryPoint;
import com.badier.badier_ride.enumeration.DeliveryStatus;
import com.badier.badier_ride.repository.AddressRepository;
import com.badier.badier_ride.repository.DeliveryPointRepository;
import com.badier.badier_ride.repository.RouteDeliveryPointRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryPointService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryPointService.class);

    private final DeliveryPointRepository deliveryPointRepository;
    private final AddressRepository addressRepository;
    private final AddressService addressService;
    private final RouteDeliveryPointRepository routeDeliveryPointRepository;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Transactional
    public DeliveryPointResponse createDeliveryPoint(DeliveryPointRequest request) {
        // Gestion de l'adresse - soit existante, soit nouvelle
        Address address;

        if (request.getAddress() != null) {
            // Création d'une nouvelle adresse
            AddressResponse createdAddress = addressService.createAddress(request.getAddress());
            address = addressRepository.findById(createdAddress.getId())
                    .orElseThrow(() -> new RuntimeException("Impossible de trouver l'adresse nouvellement créée"));
        } else if (request.getAddressId() != null) {
            // Utilisation d'une adresse existante
            address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new RuntimeException("Adresse non trouvée avec ID: " + request.getAddressId()));
        } else {
            throw new RuntimeException("Une adresse ou un ID d'adresse doit être fourni");
        }

        DeliveryPoint deliveryPoint = DeliveryPoint.builder()
                .address(address)
                .clientName(request.getClientName())
                .clientPhone(request.getClientPhoneNumber())
                .notes(request.getClientNote())
                .status(DeliveryStatus.valueOf(request.getDeliveryStatus()))
                .plannedTime(LocalDateTime.parse(request.getDeliveryTime(), DATE_TIME_FORMATTER))
                .build();

        DeliveryPoint savedDeliveryPoint = deliveryPointRepository.save(deliveryPoint);
        return mapToResponse(savedDeliveryPoint);
    }

    public DeliveryPointResponse getDeliveryPointById(Long id) {
        DeliveryPoint deliveryPoint = deliveryPointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Point de livraison non trouvé avec ID: " + id));
        return mapToResponse(deliveryPoint);
    }

    public List<DeliveryPointResponse> getAllDeliveryPoints() {
        return deliveryPointRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeliveryPointResponse updateDeliveryPoint(Long id, DeliveryPointRequest request) {
        DeliveryPoint deliveryPoint = deliveryPointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Point de livraison non trouvé avec ID: " + id));

        // Si une nouvelle adresse est fournie, on la crée
        if (request.getAddress() != null) {
            AddressResponse createdAddress = addressService.createAddress(request.getAddress());
            Address address = addressRepository.findById(createdAddress.getId())
                    .orElseThrow(() -> new RuntimeException("Impossible de trouver l'adresse nouvellement créée"));
            deliveryPoint.setAddress(address);
        }
        // Sinon si un ID d'adresse est fourni et qu'il est différent de l'adresse
        // actuelle
        else if (request.getAddressId() != null && !request.getAddressId().equals(deliveryPoint.getAddress().getId())) {
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new RuntimeException("Adresse non trouvée avec ID: " + request.getAddressId()));
            deliveryPoint.setAddress(address);
        }

        deliveryPoint.setClientName(request.getClientName());
        deliveryPoint.setClientPhone(request.getClientPhoneNumber());
        deliveryPoint.setNotes(request.getClientNote());
        deliveryPoint.setStatus(DeliveryStatus.valueOf(request.getDeliveryStatus()));
        deliveryPoint.setPlannedTime(LocalDateTime.parse(request.getDeliveryTime(), DATE_TIME_FORMATTER));

        DeliveryPoint updatedDeliveryPoint = deliveryPointRepository.save(deliveryPoint);
        return mapToResponse(updatedDeliveryPoint);
    }

    /**
     * @deprecated Utiliser updateStatusInRoute() à la place pour un statut
     *             spécifique à une tournée
     */
    @Deprecated
    @Transactional
    public DeliveryPointResponse updateStatus(Long id, DeliveryStatus status) {
        DeliveryPoint deliveryPoint = deliveryPointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Point de livraison non trouvé avec ID: " + id));

        deliveryPoint.setStatus(status);

        // Si le statut passe à COMPLETED, on enregistre le moment
        if (status == DeliveryStatus.COMPLETED) {
            deliveryPoint.setActualTime(LocalDateTime.now());
        }

        DeliveryPoint updatedDeliveryPoint = deliveryPointRepository.save(deliveryPoint);
        return mapToResponse(updatedDeliveryPoint);
    }

    /**
     * Met à jour le statut d'un point de livraison dans le contexte d'une tournée
     * spécifique.
     * Le statut est stocké dans RouteDeliveryPoint, permettant à un même point
     * d'avoir
     * des statuts différents dans différentes tournées.
     */
    @Transactional
    public DeliveryPointResponse updateStatusInRoute(Long routeId, Long deliveryPointId, DeliveryStatus status) {
        // Récupérer le RouteDeliveryPoint (relation entre route et delivery point)
        RouteDeliveryPoint rdp = resolveRouteDeliveryPoint(routeId, deliveryPointId);

        // Mettre à jour le statut dans la relation (pas dans le DeliveryPoint global)
        rdp.setStatus(status);

        // Si le statut passe à COMPLETED ou FAILED, on enregistre le moment
        if (status == DeliveryStatus.COMPLETED || status == DeliveryStatus.FAILED) {
            rdp.setActualTime(LocalDateTime.now());
        }

        RouteDeliveryPoint updatedRdp = routeDeliveryPointRepository.save(rdp);

        // Retourner la réponse basée sur le DeliveryPoint
        return mapToResponse(updatedRdp.getDeliveryPoint());
    }

    private RouteDeliveryPoint resolveRouteDeliveryPoint(Long routeId, Long deliveryPointId) {
        List<RouteDeliveryPoint> matches = routeDeliveryPointRepository
                .findAllByRouteIdAndDeliveryPointIdOrderBySequenceOrderAsc(routeId, deliveryPointId);

        if (matches.isEmpty()) {
            throw new RuntimeException(
                    String.format("Point de livraison %d non trouvé dans la tournée %d", deliveryPointId, routeId));
        }

        if (matches.size() > 1) {
            log.warn(
                    "{} doublons détectés pour la tournée {} et le point {}. Nettoyage automatique des entrées en trop.",
                    matches.size() - 1, routeId, deliveryPointId);
            routeDeliveryPointRepository.deleteAll(matches.subList(1, matches.size()));
        }

        return matches.get(0);
    }

    @Transactional
    public void deleteDeliveryPoint(Long id) {
        deliveryPointRepository.deleteById(id);
    }

    public List<DeliveryPointResponse> getDeliveryPointsByStatus(DeliveryStatus status) {
        return deliveryPointRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DeliveryPointResponse mapToResponse(DeliveryPoint deliveryPoint) {
        return DeliveryPointResponse.builder()
                .id(deliveryPoint.getId())
                .address(addressService.getAddressResponseById(deliveryPoint.getAddress().getId()))
                .clientName(deliveryPoint.getClientName())
                .clientPhoneNumber(deliveryPoint.getClientPhone())
                .clientNote(deliveryPoint.getNotes())
                .deliveryNote(deliveryPoint.getNotes())
                .deliveryTime(deliveryPoint.getPlannedTime().toString())
                .deliveryDate(deliveryPoint.getPlannedTime().toString())
                .deliveryStatus(deliveryPoint.getStatus().toString())
                .plannedTime(deliveryPoint.getPlannedTime())
                .actualTime(deliveryPoint.getActualTime())
                .sequenceOrder(null)
                .isStartPoint(Boolean.FALSE)
                .isEndPoint(Boolean.FALSE)
                .build();
    }

    public DeliveryPointResponse createDeliveryPointFromAddress(Long addressId, DeliveryPointRequest defaultValues) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + addressId));

        DeliveryPoint deliveryPoint = DeliveryPoint.builder()
                .address(address)
                .clientName(defaultValues.getClientName() != null ? defaultValues.getClientName() : "Client")
                .clientPhone(defaultValues.getClientPhoneNumber())
                .notes(defaultValues.getClientNote())
                .status(DeliveryStatus.PENDING)
                .plannedTime(defaultValues.getDeliveryTime() != null
                        ? LocalDateTime.parse(defaultValues.getDeliveryTime(), DATE_TIME_FORMATTER)
                        : LocalDateTime.now().plusDays(1))
                .build();

        DeliveryPoint savedDeliveryPoint = deliveryPointRepository.save(deliveryPoint);
        return mapToResponse(savedDeliveryPoint);
    }
}