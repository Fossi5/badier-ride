package com.badier.badier_ride.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.badier.badier_ride.dto.DispatcherProfileRequest;
import com.badier.badier_ride.dto.DispatcherResponse;
import com.badier.badier_ride.entity.Dispatcher;
import com.badier.badier_ride.repository.DispatcherRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DispatcherService {

    private final DispatcherRepository dispatcherRepository;

    public DispatcherResponse getDispatcherProfile(String username) {
        Dispatcher dispatcher = dispatcherRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Répartiteur non trouvé avec username: " + username));
        return mapToResponse(dispatcher);
    }

    @Transactional
    public DispatcherResponse updateDispatcherProfile(String username, DispatcherProfileRequest request) {
        Dispatcher dispatcher = dispatcherRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Répartiteur non trouvé avec username: " + username));

        // Le répartiteur ne peut mettre à jour que des informations limitées
        // Ici, il peut seulement mettre à jour son département
        if (request.getDepartment() != null) {
            dispatcher.setDepartment(request.getDepartment());
        }

        Dispatcher updatedDispatcher = dispatcherRepository.save(dispatcher);
        return mapToResponse(updatedDispatcher);
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
