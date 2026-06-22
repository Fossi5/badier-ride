package com.badier.badier_ride.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badier.badier_ride.dto.AddressResponse;
import com.badier.badier_ride.dto.DeliveryPointRequest;
import com.badier.badier_ride.dto.DeliveryPointResponse;
import com.badier.badier_ride.exception.InvalidOperationException;
import com.badier.badier_ride.exception.ResourceNotFoundException;
import com.badier.badier_ride.entity.Address;
import com.badier.badier_ride.entity.DeliveryPoint;
import com.badier.badier_ride.entity.RouteDeliveryPoint;
import com.badier.badier_ride.enumeration.DeliveryStatus;
import com.badier.badier_ride.enumeration.NotificationType;
import com.badier.badier_ride.repository.AddressRepository;
import com.badier.badier_ride.repository.DeliveryPointRepository;
import com.badier.badier_ride.repository.RouteDeliveryPointRepository;
import com.badier.badier_ride.repository.RouteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryPointService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryPointService.class);

    private final DeliveryPointRepository deliveryPointRepository;
    private final AddressRepository addressRepository;
    private final AddressService addressService;
    private final RouteDeliveryPointRepository routeDeliveryPointRepository;
    private final RouteRepository routeRepository;
    private final NotificationService notificationService;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Transactional
    public DeliveryPointResponse createDeliveryPoint(DeliveryPointRequest request) {
        Address address;

        if (request.getAddress() != null) {
            AddressResponse createdAddress = addressService.createAddress(request.getAddress());
            address = addressRepository.findById(createdAddress.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Impossible de trouver l'adresse nouvellement créée"));
        } else if (request.getAddressId() != null) {
            address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Adresse non trouvée avec ID: " + request.getAddressId()));
        } else {
            throw new InvalidOperationException("Une adresse ou un ID d'adresse doit être fourni");
        }

        DeliveryPoint deliveryPoint = DeliveryPoint.builder()
                .address(address)
                .clientName(request.getClientName())
                .clientPhone(request.getClientPhoneNumber())
                .clientEmail(request.getClientEmail())
                .clientNote(request.getClientNote())
                .deliveryNote(request.getDeliveryNote())
                .status(request.getDeliveryStatus() != null ? DeliveryStatus.valueOf(request.getDeliveryStatus()) : DeliveryStatus.PENDING)
                .plannedTime(request.getDeliveryTime() != null ? LocalDateTime.parse(request.getDeliveryTime(), DATE_TIME_FORMATTER) : null)
                .build();

        DeliveryPoint savedDeliveryPoint = deliveryPointRepository.save(deliveryPoint);
        return mapToResponse(savedDeliveryPoint);
    }

    public DeliveryPointResponse getDeliveryPointById(Long id) {
        DeliveryPoint deliveryPoint = deliveryPointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Point de livraison non trouvé avec ID: " + id));
        return mapToResponse(deliveryPoint);
    }

    public List<DeliveryPointResponse> getAllDeliveryPoints() {
        return deliveryPointRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<DeliveryPointResponse> getVerifiedDeliveryPoints() {
        return deliveryPointRepository.findByAddressIsVerifiedTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<DeliveryPointResponse> getAllDeliveryPointsPaged(int page, int size) {
        return deliveryPointRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()))
                .map(this::mapToResponse);
    }

    @Transactional
    public DeliveryPointResponse updateDeliveryPoint(Long id, DeliveryPointRequest request) {
        DeliveryPoint deliveryPoint = deliveryPointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Point de livraison non trouvé avec ID: " + id));

        if (request.getAddress() != null) {
            Long existingAddressId = deliveryPoint.getAddress() != null ? deliveryPoint.getAddress().getId() : null;
            if (existingAddressId != null) {
                addressService.updateAddress(existingAddressId, request.getAddress());
            } else {
                AddressResponse createdAddress = addressService.createAddress(request.getAddress());
                Address address = addressRepository.findById(createdAddress.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Adresse introuvable"));
                deliveryPoint.setAddress(address);
            }
        } else if (request.getAddressId() != null && !request.getAddressId().equals(deliveryPoint.getAddress().getId())) {
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Adresse non trouvée avec ID: " + request.getAddressId()));
            deliveryPoint.setAddress(address);
        }

        deliveryPoint.setClientName(request.getClientName());
        deliveryPoint.setClientPhone(request.getClientPhoneNumber());
        deliveryPoint.setClientEmail(request.getClientEmail());
        deliveryPoint.setClientNote(request.getClientNote());
        deliveryPoint.setDeliveryNote(request.getDeliveryNote());
        if (request.getDeliveryStatus() != null) {
            deliveryPoint.setStatus(DeliveryStatus.valueOf(request.getDeliveryStatus()));
        }
        if (request.getDeliveryTime() != null) {
            deliveryPoint.setPlannedTime(LocalDateTime.parse(request.getDeliveryTime(), DATE_TIME_FORMATTER));
        }

        DeliveryPoint updatedDeliveryPoint = deliveryPointRepository.save(deliveryPoint);
        return mapToResponse(updatedDeliveryPoint);
    }

    @Transactional
    public DeliveryPointResponse updateStatusInRoute(Long routeId, Long deliveryPointId, DeliveryStatus status) {
        RouteDeliveryPoint rdp = resolveRouteDeliveryPoint(routeId, deliveryPointId);

        rdp.setStatus(status);

        if (status == DeliveryStatus.COMPLETED || status == DeliveryStatus.FAILED) {
            rdp.setActualTime(LocalDateTime.now());
        }

        RouteDeliveryPoint updatedRdp = routeDeliveryPointRepository.save(rdp);

        if (status == DeliveryStatus.COMPLETED || status == DeliveryStatus.FAILED) {
            routeRepository.findById(routeId).ifPresent(route -> {
                String clientName = rdp.getDeliveryPoint().getClientName();
                String msg = status == DeliveryStatus.COMPLETED
                        ? "Livraison confirmée : " + clientName
                        : "Échec de livraison : " + clientName;
                notificationService.send(
                    route.getDispatcher(),
                    route.getDriver(),
                    status == DeliveryStatus.COMPLETED ? NotificationType.ROUTE_UPDATE : NotificationType.ALERT,
                    msg
                );
            });
        }

        return mapToResponse(updatedRdp);
    }

    private RouteDeliveryPoint resolveRouteDeliveryPoint(Long routeId, Long deliveryPointId) {
        List<RouteDeliveryPoint> matches = routeDeliveryPointRepository
                .findAllByRouteIdAndDeliveryPointIdOrderBySequenceOrderAsc(routeId, deliveryPointId);

        if (matches.isEmpty()) {
            throw new ResourceNotFoundException(
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
                .clientEmail(deliveryPoint.getClientEmail())
                .clientNote(deliveryPoint.getClientNote())
                .deliveryNote(deliveryPoint.getDeliveryNote())
                .deliveryTime(deliveryPoint.getPlannedTime() != null ? deliveryPoint.getPlannedTime().toLocalTime().toString() : null)
                .deliveryDate(deliveryPoint.getPlannedTime() != null ? deliveryPoint.getPlannedTime().toLocalDate().toString() : null)
                .deliveryStatus(deliveryPoint.getStatus() != null ? deliveryPoint.getStatus() : DeliveryStatus.PENDING)
                .plannedTime(deliveryPoint.getPlannedTime())
                .actualTime(deliveryPoint.getActualTime())
                .build();
    }

    public DeliveryPointResponse mapToResponse(RouteDeliveryPoint rdp) {
        DeliveryPointResponse base = mapToResponse(rdp.getDeliveryPoint());
        base.setSequenceOrder(rdp.getSequenceOrder());
        base.setIsStartPoint(Boolean.TRUE.equals(rdp.getIsStartPoint()));
        base.setIsEndPoint(Boolean.TRUE.equals(rdp.getIsEndPoint()));
        base.setDeliveryStatus(rdp.getStatus() != null ? rdp.getStatus() : base.getDeliveryStatus());
        if (rdp.getPlannedTime() != null) {
            base.setPlannedTime(rdp.getPlannedTime());
            base.setDeliveryTime(rdp.getPlannedTime().toLocalTime().toString());
            base.setDeliveryDate(rdp.getPlannedTime().toLocalDate().toString());
        }
        if (rdp.getActualTime() != null) {
            base.setActualTime(rdp.getActualTime());
        }
        base.setProofImagePath(rdp.getProofImagePath());
        base.setProofValidated(Boolean.TRUE.equals(rdp.getProofValidated()));
        base.setHasConfirmationCode(rdp.getConfirmationCode() != null);
        return base;
    }

    public DeliveryPointResponse createDeliveryPointFromAddress(Long addressId, DeliveryPointRequest defaultValues) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        DeliveryPoint deliveryPoint = DeliveryPoint.builder()
                .address(address)
                .clientName(defaultValues.getClientName() != null ? defaultValues.getClientName() : "Client")
                .clientPhone(defaultValues.getClientPhoneNumber())
                .clientNote(defaultValues.getClientNote())
                .status(DeliveryStatus.PENDING)
                .plannedTime(defaultValues.getDeliveryTime() != null
                        ? LocalDateTime.parse(defaultValues.getDeliveryTime(), DATE_TIME_FORMATTER)
                        : LocalDateTime.now().plusDays(1))
                .build();

        DeliveryPoint savedDeliveryPoint = deliveryPointRepository.save(deliveryPoint);
        return mapToResponse(savedDeliveryPoint);
    }
}