package com.badier.badier_ride.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.badier.badier_ride.entity.User;
import com.badier.badier_ride.enumeration.UserRole;
import com.badier.badier_ride.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Vérifier si l'admin existe déjà
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("password"))
                    .email("admin@badierride.com")
                    .role(UserRole.ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println("Admin user created");
        }
    }
}