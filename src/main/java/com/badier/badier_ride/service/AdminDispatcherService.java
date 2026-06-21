package com.badier.badier_ride.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.badier.badier_ride.dto.DispatcherRequest;
import com.badier.badier_ride.dto.DispatcherResponse;
import com.badier.badier_ride.entity.Dispatcher;
import com.badier.badier_ride.entity.User;
import com.badier.badier_ride.enumeration.UserRole;
import com.badier.badier_ride.exception.InvalidOperationException;
import com.badier.badier_ride.exception.ResourceNotFoundException;
import com.badier.badier_ride.repository.DispatcherRepository;
import com.badier.badier_ride.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDispatcherService {

    private final DispatcherRepository dispatcherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public DispatcherResponse createDispatcher(DispatcherRequest request) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidOperationException("Le mot de passe est obligatoire");
        }
        if (userRepository.existsByUsernameAndActiveTrue(request.getUsername())) {
            throw new InvalidOperationException("Ce nom d'utilisateur est déjà utilisé");
        }
        if (userRepository.existsByEmailAndActiveTrue(request.getEmail())) {
            throw new InvalidOperationException("Cet email est déjà utilisé");
        }

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setUsername(request.getUsername());
        dispatcher.setPassword(passwordEncoder.encode(request.getPassword()));
        dispatcher.setEmail(request.getEmail());
        dispatcher.setRole(UserRole.DISPATCHER);
        dispatcher.setDepartment(request.getDepartment());

        Dispatcher savedDispatcher = dispatcherRepository.save(dispatcher);
        return mapToResponse(savedDispatcher);
    }

    public DispatcherResponse getDispatcherById(Long id) {
        Dispatcher dispatcher = dispatcherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Répartiteur non trouvé avec ID: " + id));
        return mapToResponse(dispatcher);
    }

    public List<DispatcherResponse> getAllDispatchers() {
        return dispatcherRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<DispatcherResponse> getAllDispatchersPaged(int page, int size) {
        return dispatcherRepository.findByActiveTrue(PageRequest.of(page, size, Sort.by("id").descending()))
                .map(this::mapToResponse);
    }

    @Transactional
    public DispatcherResponse updateDispatcher(Long id, DispatcherRequest request) {
        Dispatcher dispatcher = dispatcherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Répartiteur non trouvé avec ID: " + id));

        if (request.getUsername() != null) dispatcher.setUsername(request.getUsername());
        if (request.getEmail() != null) dispatcher.setEmail(request.getEmail());
        if (request.getPassword() != null) dispatcher.setPassword(passwordEncoder.encode(request.getPassword()));
        if (request.getDepartment() != null) dispatcher.setDepartment(request.getDepartment());

        Dispatcher updatedDispatcher = dispatcherRepository.save(dispatcher);
        return mapToResponse(updatedDispatcher);
    }

    @Transactional
    public void deleteDispatcher(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatcher", id));
        // Libérer username/email pour permettre la recréation avec les mêmes coordonnées
        user.setUsername(user.getUsername() + "_deleted_" + id);
        user.setEmail(user.getEmail() + "_deleted_" + id);
        user.setActive(false);
        userRepository.save(user);
    }

    public List<DispatcherResponse> getDispatchersByDepartment(String department) {
        return dispatcherRepository.findByDepartment(department).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DispatcherResponse mapToResponse(Dispatcher dispatcher) {
        DispatcherResponse response = new DispatcherResponse();
        response.setId(dispatcher.getId());
        response.setUsername(dispatcher.getUsername());
        response.setEmail(dispatcher.getEmail());
        response.setDepartment(dispatcher.getDepartment());
        return response;
    }
}
