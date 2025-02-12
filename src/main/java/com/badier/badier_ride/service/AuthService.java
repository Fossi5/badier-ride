package com.badier.badier_ride.service;

import com.badier.badier_ride.repository.UserRepository;
import com.badier.badier_ride.dto.AuthRequest;
import com.badier.badier_ride.dto.AuthResponse;
import com.badier.badier_ride.dto.RegisterRequest;
import com.badier.badier_ride.entity.User;
import com.badier.badier_ride.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // Vérifier si l'utilisateur existe déjà
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepository.save(user);

        var jwt = jwtUtil.generateToken(user);
        
        return AuthResponse.builder()
                .token(jwt)
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var jwt = jwtUtil.generateToken(user);
        
        return AuthResponse.builder()
                .token(jwt)
                .role(user.getRole().name())
                .build();
    }
}