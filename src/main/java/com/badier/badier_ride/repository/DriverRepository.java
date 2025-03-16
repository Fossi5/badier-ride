package com.badier.badier_ride.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.badier.badier_ride.entity.Driver;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByUsername(String username);
    List<Driver> findByIsAvailable(Boolean isAvailable);
    Optional<Driver> findByPhoneNumber(String phoneNumber);
    List<Driver> findByVehicleType(String vehicleType);
}