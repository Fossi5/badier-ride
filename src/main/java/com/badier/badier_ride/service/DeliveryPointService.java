package com.badier.badier_ride.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.badier.badier_ride.dto.DeliveryPointRequest;
import com.badier.badier_ride.dto.DeliveryPointResponse;
import com.badier.badier_ride.entity.Address;
import com.badier.badier_ride.entity.DeliveryPoint;
import com.badier.badier_ride.enumeration.DeliveryStatus;
import com.badier.badier_ride.repository.AddressRepository;
import com.badier.badier_ride.repository.DeliveryPointRepository;
import com.badier.badier_ride.service.AddressService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryPointService {

    private final DeliveryPointRepository deliveryPointRepository;
    private final AddressRepository addressRepository;
    private final AddressService addressService;

    public DeliveryPointResponse createDeliveryPoint(DeliveryPointRequest request) {
        // Trouver l'adresse
        Address address = addressRepository.findById(request.getAddressId())
            .orElseThrow(() -> new RuntimeException("Adresse non trouvée avec ID: " + request.getAddressId()));

        // Créer le point de livraison
        DeliveryPoint deliveryPoint = DeliveryPoint.builder()
            .address(address)
            .clientName(request.getClientName())
            .clientPhone(request.getClientPhoneNumber())
            .notes(request.getClientNote())
            .status(DeliveryStatus.valueOf(request.getDeliveryStatus()))
            .plannedTime(LocalDateTime.parse(request.getDeliveryTime())) // ou une autre logique pour plannedTime
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
            .toList();
    }

    public DeliveryPointResponse updateDeliveryPoint(Long id, DeliveryPointRequest request) {
        DeliveryPoint deliveryPoint = deliveryPointRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Point de livraison non trouvé avec ID: " + id));

        // Mise à jour de l'adresse si nécessaire
        if (request.getAddressId() != null && !request.getAddressId().equals(deliveryPoint.getAddress().getId())) {
            Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new RuntimeException("Adresse non trouvée avec ID: " + request.getAddressId()));
            deliveryPoint.setAddress(address);
        }

        // Mise à jour des autres champs
        deliveryPoint.setClientName(request.getClientName());
        deliveryPoint.setClientPhone(request.getClientPhoneNumber());
        deliveryPoint.setNotes(request.getClientNote());
        deliveryPoint.setStatus(DeliveryStatus.valueOf(request.getDeliveryStatus()));
        deliveryPoint.setPlannedTime(LocalDateTime.parse(request.getDeliveryTime())); // ou une autre logique pour plannedTime

        DeliveryPoint updatedDeliveryPoint = deliveryPointRepository.save(deliveryPoint);
        return mapToResponse(updatedDeliveryPoint);
    }

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

    public void deleteDeliveryPoint(Long id) {
        deliveryPointRepository.deleteById(id);
    }

    public List<DeliveryPointResponse> getDeliveryPointsByStatus(DeliveryStatus status) {
        return deliveryPointRepository.findByStatus(status).stream()
            .map(this::mapToResponse)
            .toList();
    }

    // Méthode privée pour mapper l'entité vers DTO
    private DeliveryPointResponse mapToResponse(DeliveryPoint deliveryPoint) {
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
            .build();
    }
}