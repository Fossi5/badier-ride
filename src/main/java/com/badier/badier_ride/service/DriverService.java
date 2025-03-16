package com.badier.badier_ride.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.badier.badier_ride.dto.DriverProfileRequest;
import com.badier.badier_ride.dto.DriverResponse;
import com.badier.badier_ride.entity.Driver;
import com.badier.badier_ride.entity.Location;
import com.badier.badier_ride.repository.DriverRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverResponse getDriverProfile(String username) {
        Driver driver = driverRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Chauffeur non trouvé avec username: " + username));
        return mapToResponse(driver);
    }

    @Transactional
    public DriverResponse updateDriverProfile(String username, DriverProfileRequest request) {
        Driver driver = driverRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Chauffeur non trouvé avec username: " + username));

        // Le chauffeur ne peut mettre à jour que certaines informations limitées
        if (request.getPhoneNumber() != null) driver.setPhoneNumber(request.getPhoneNumber());
        
        // Mise à jour de la localisation
        if (request.getLatitude() != null && request.getLongitude() != null) {
            Location location = driver.getCurrentLocation();
            if (location == null) {
                location = new Location();
                driver.setCurrentLocation(location);
            }
            location.setLatitude(request.getLatitude());
            location.setLongitude(request.getLongitude());
            location.setTimestamp(java.time.LocalDateTime.now());
        }

        Driver updatedDriver = driverRepository.save(driver);
        return mapToResponse(updatedDriver);
    }

    @Transactional
    public DriverResponse updateDriverAvailability(String username, Boolean isAvailable) {
        Driver driver = driverRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Chauffeur non trouvé avec username: " + username));

        driver.setIsAvailable(isAvailable);
        Driver updatedDriver = driverRepository.save(driver);
        return mapToResponse(updatedDriver);
    }

    private DriverResponse mapToResponse(Driver driver) {
        DriverResponse response = new DriverResponse();
        response.setId(driver.getId());
        response.setUsername(driver.getUsername());
        response.setEmail(driver.getEmail());
        response.setPhoneNumber(driver.getPhoneNumber());
        response.setVehicleType(driver.getVehicleType());
        response.setIsAvailable(driver.getIsAvailable());
        
        if (driver.getCurrentLocation() != null) {
            response.setLatitude(driver.getCurrentLocation().getLatitude());
            response.setLongitude(driver.getCurrentLocation().getLongitude());
            if (driver.getCurrentLocation().getTimestamp() != null) {
                response.setLastLocationUpdate(driver.getCurrentLocation().getTimestamp().toString());
            }
        }
        
        return response;
    }
}