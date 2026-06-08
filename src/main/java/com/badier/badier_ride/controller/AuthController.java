package com.badier.badier_ride.controller;

import com.badier.badier_ride.dto.AuthRequest;
import com.badier.badier_ride.dto.AuthResponse;
import com.badier.badier_ride.dto.RegisterRequest;
import com.badier.badier_ride.exception.AppException;
import com.badier.badier_ride.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse httpResponse) {
        try {
            AuthResponse response = authService.register(request);
            setJwtCookie(httpResponse, response.getToken());

            Map<String, String> body = new HashMap<>();
            body.put("username", response.getUsername());
            body.put("role", response.getRole());
            return ResponseEntity.ok(body);
        } catch (AppException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@Valid @RequestBody AuthRequest request, HttpServletResponse httpResponse) {
        try {
            AuthResponse response = authService.authenticate(request);
            setJwtCookie(httpResponse, response.getToken());

            Map<String, String> body = new HashMap<>();
            body.put("username", response.getUsername());
            body.put("role", response.getRole());
            return ResponseEntity.ok(body);
        } catch (AppException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse httpResponse) {
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        httpResponse.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    private void setJwtCookie(HttpServletResponse response, String token) {
        // Utiliser l'en-tête Set-Cookie directement pour inclure SameSite
        // (l'API Cookie Java standard ne supporte pas l'attribut SameSite)
        String cookieValue = "jwt=" + token
                + "; HttpOnly"
                + "; Path=/"
                + "; Max-Age=86400"
                + "; SameSite=Strict";
        // Secure=false en développement (HTTP) ; passer à true en production (HTTPS)
        response.addHeader("Set-Cookie", cookieValue);
    }
}
