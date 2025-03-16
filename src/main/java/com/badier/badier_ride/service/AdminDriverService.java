package com.badier.badier_ride.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.badier.badier_ride.dto.DriverRequest;
import com.badier.badier_ride.dto.DriverResponse;
import com.badier.badier_ride.entity.Driver;
import com.badier.badier_ride.entity.Location;
import com.badier.badier_ride.enumeration.UserRole;
import com.badier.badier_ride.repository.DriverRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDriverService {

    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public DriverResponse createDriver(DriverRequest request) {
        // Créer un nouvel objet Location si des coordonnées sont fournies
        Location location = null;
        if (request.getLatitude() != null && request.getLongitude() != null) {
            location = new Location();
            location.setLatitude(request.getLatitude());
            location.setLongitude(request.getLongitude());
            location.setTimestamp(java.time.LocalDateTime.now());
        }

        Driver driver = new Driver();
        driver.setUsername(request.getUsername());
        driver.setPassword(passwordEncoder.encode(request.getPassword()));
        driver.setEmail(request.getEmail());
        driver.setRole(UserRole.DRIVER);
        driver.setPhoneNumber(request.getPhoneNumber());
        driver.setVehicleType(request.getVehicleType());
        driver.setIsAvailable(true);
        driver.setCurrentLocation(location);

        Driver savedDriver = driverRepository.save(driver);
        return mapToResponse(savedDriver);
    }

    public DriverResponse getDriverById(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé avec ID: " + id));
        return mapToResponse(driver);
    }

    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DriverResponse updateDriver(Long id, DriverRequest request) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé avec ID: " + id));

        if (request.getUsername() != null) driver.setUsername(request.getUsername());
        if (request.getEmail() != null) driver.setEmail(request.getEmail());
        if (request.getPassword() != null) driver.setPassword(passwordEncoder.encode(request.getPassword()));
        if (request.getPhoneNumber() != null) driver.setPhoneNumber(request.getPhoneNumber());
        if (request.getVehicleType() != null) driver.setVehicleType(request.getVehicleType());
        if (request.getIsAvailable() != null) driver.setIsAvailable(request.getIsAvailable());

        // Mise à jour de la localisation si des coordonnées sont fournies
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
    public void deleteDriver(Long id) {
        driverRepository.deleteById(id);
    }

    public List<DriverResponse> getAvailableDrivers() {
        return driverRepository.findByIsAvailable(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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