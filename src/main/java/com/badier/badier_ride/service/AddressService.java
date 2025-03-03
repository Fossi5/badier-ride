package com.badier.badier_ride.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.badier.badier_ride.dto.AddressRequest;
import com.badier.badier_ride.dto.AddressResponse;
import com.badier.badier_ride.entity.Address;
import com.badier.badier_ride.repository.AddressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    
    public AddressResponse createAddress(AddressRequest request) {
        // Vérifier si l'adresse existe déjà
        Optional<Address> existingAddress = addressRepository.findByStreetAndCityAndPostalCodeAndCountry(
            request.getStreet(), 
            request.getCity(), 
            request.getPostalCode(), 
            request.getCountry()
        );
        
        if (existingAddress.isPresent()) {
            return mapToResponse(existingAddress.get());
        }
        
        // Créer une nouvelle adresse
        Address address = Address.builder()
            .street(request.getStreet())
            .city(request.getCity())
            .postalCode(request.getPostalCode())
            .country(request.getCountry())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .isVerified(false)
            .build();
        
        // Si les coordonnées ne sont pas fournies, on pourrait appeler un service de géocodage ici
        
        Address savedAddress = addressRepository.save(address);
        return mapToResponse(savedAddress);
    }
    
    public AddressResponse getAddressById(Long id) {
        Address address = addressRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Adresse non trouvée avec ID: " + id));
        
        return mapToResponse(address);
    }
    
    public List<AddressResponse> getAllAddresses() {
        return addressRepository.findAll().stream()
            .map(this::mapToResponse)
            .toList();
    }
    
    public AddressResponse updateAddress(Long id, AddressRequest request) {
        Address address = addressRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Adresse non trouvée avec ID: " + id));
        
        // Mettre à jour les champs
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        
        // Marquer comme non vérifié après modification
        address.setIsVerified(false);
        
        Address updatedAddress = addressRepository.save(address);
        return mapToResponse(updatedAddress);
    }
    
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }
    
    public List<AddressResponse> findPotentialDuplicates(AddressRequest request) {
        return addressRepository.findPotentialDuplicates(
            request.getStreet(), 
            request.getCity(),
            request.getPostalCode()
        ).stream()
        .map(this::mapToResponse)
        .toList();
    }
    
    // Méthode privée pour mapper de l'entité vers DTO
    private AddressResponse mapToResponse(Address address) {
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