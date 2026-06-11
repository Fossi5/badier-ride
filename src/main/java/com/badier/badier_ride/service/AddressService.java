package com.badier.badier_ride.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.badier.badier_ride.dto.AddressRequest;
import com.badier.badier_ride.dto.AddressResponse;
import com.badier.badier_ride.entity.Address;
import com.badier.badier_ride.exception.InvalidOperationException;
import com.badier.badier_ride.exception.ResourceNotFoundException;
import com.badier.badier_ride.repository.AddressRepository;
import com.badier.badier_ride.repository.DeliveryPointRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final DeliveryPointRepository deliveryPointRepository;

    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        Address address = Address.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isVerified(false)
                .build();

        Address savedAddress = addressRepository.save(address);
        return mapToResponse(savedAddress);
    }

    public AddressResponse getAddressById(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adresse non trouvée avec ID: " + id));
        return mapToResponse(address);
    }

    public AddressResponse getAddressResponseById(Long id) {
        return getAddressById(id);
    }

    public List<AddressResponse> getAllAddresses() {
        return addressRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<AddressResponse> getAllAddressesPaged(int page, int size) {
        return addressRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()))
                .map(this::mapToResponse);
    }

    @Transactional
    public AddressResponse updateAddress(Long id, AddressRequest request) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adresse non trouvée avec ID: " + id));

        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setIsVerified(false); // Reset verification status after update

        Address updatedAddress = addressRepository.save(address);
        return mapToResponse(updatedAddress);
    }

    @Transactional
    public void deleteAddress(Long id) {
        if (deliveryPointRepository.existsByAddressId(id)) {
            throw new InvalidOperationException("Cette adresse est utilisée par un ou plusieurs points de livraison");
        }
        addressRepository.deleteById(id);
    }

    public List<AddressResponse> findAddressesByCity(String city) {
        return addressRepository.findByCity(city).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AddressResponse> findPotentialDuplicates(AddressRequest request) {
        return addressRepository.findPotentialDuplicates(
                request.getStreet(),
                request.getCity(),
                request.getPostalCode()
        ).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .isVerified(address.getIsVerified())
                .build();
    }
}