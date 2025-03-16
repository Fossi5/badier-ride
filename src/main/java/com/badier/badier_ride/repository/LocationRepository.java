package com.badier.badier_ride.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.badier.badier_ride.entity.Location;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    @Query("SELECT l FROM Location l WHERE " +
           "ABS(l.latitude - :latitude) < :distance AND " +
           "ABS(l.longitude - :longitude) < :distance")
    List<Location> findNearbyLocations(Double latitude, Double longitude, Double distance);
}
