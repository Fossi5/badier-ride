package com.badier.badier_ride.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.badier.badier_ride.entity.Dispatcher;

@Repository
public interface DispatcherRepository extends JpaRepository<Dispatcher, Long> {
    Optional<Dispatcher> findByUsername(String username);
    List<Dispatcher> findByDepartment(String department);
}