package com.badier.badier_ride.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.badier.badier_ride.entity.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    // Recherche d'adresses par ville
    List<Address> findByCity(String city);
    
    // Recherche d'adresses par code postal
    List<Address> findByPostalCode(String postalCode);
    
    // Recherche d'adresse exacte pour éviter les doublons
    Optional<Address> findByStreetAndCityAndPostalCodeAndCountry(
        String street, String city, String postalCode, String country);
    
    // Recherche de doublons potentiels
    @Query("SELECT a FROM Address a WHERE " +
           "LOWER(a.street) LIKE LOWER(CONCAT('%', :street, '%')) AND " +
           "LOWER(a.city) = LOWER(:city) AND " +
           "a.postalCode = :postalCode")
    List<Address> findPotentialDuplicates(String street, String city, String postalCode);
    
    // Recherche par coordonnées géographiques proches
    @Query("SELECT a FROM Address a WHERE " +
           "ABS(a.latitude - :latitude) < 0.001 AND " +
           "ABS(a.longitude - :longitude) < 0.001")
    List<Address> findNearbyAddresses(Double latitude, Double longitude);
}
