package com.badier.badier_ride.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.badier.badier_ride.dto.DriverRequest;
import com.badier.badier_ride.dto.DriverResponse;
import com.badier.badier_ride.entity.Driver;
import com.badier.badier_ride.entity.Location;
import com.badier.badier_ride.entity.User;
import com.badier.badier_ride.enumeration.UserRole;
import com.badier.badier_ride.exception.InvalidOperationException;
import com.badier.badier_ride.exception.ResourceNotFoundException;
import com.badier.badier_ride.repository.DriverRepository;
import com.badier.badier_ride.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public DriverResponse createDriver(DriverRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new InvalidOperationException("Ce nom d'utilisateur est déjà utilisé");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidOperationException("Cet email est déjà utilisé");
        }

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

        emailService.send(
            savedDriver.getEmail(),
            "Bienvenue sur Badier Ride",
            "Bonjour " + savedDriver.getUsername() + ",\n\n" +
            "Votre compte chauffeur a été créé.\n" +
            "Connectez-vous avec votre nom d'utilisateur pour commencer."
        );

        return mapToResponse(savedDriver);
    }

    public DriverResponse getDriverById(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé avec ID: " + id));
        return mapToResponse(driver);
    }

    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<DriverResponse> getAllDriversPaged(int page, int size) {
        return driverRepository.findByActiveTrue(PageRequest.of(page, size, Sort.by("id").descending()))
                .map(this::mapToResponse);
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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", id));
        user.setActive(false);
        userRepository.save(user);
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