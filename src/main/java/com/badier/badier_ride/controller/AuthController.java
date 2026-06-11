package com.badier.badier_ride.controller;

import com.badier.badier_ride.dto.AuthRequest;
import com.badier.badier_ride.dto.AuthResponse;
import com.badier.badier_ride.dto.RefreshTokenRequest;
import com.badier.badier_ride.dto.RegisterRequest;
import com.badier.badier_ride.entity.RefreshToken;
import com.badier.badier_ride.entity.User;
import com.badier.badier_ride.exception.AppException;
import com.badier.badier_ride.security.JwtUtil;
import com.badier.badier_ride.service.AuthService;
import com.badier.badier_ride.service.RefreshTokenService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse httpResponse) {
        try {
            AuthResponse response = authService.register(request);
            User user = authService.loadUser(response.getUsername());
            setJwtCookie(httpResponse, response.getToken());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
            setRefreshCookie(httpResponse, refreshToken.getToken());

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
            User user = authService.loadUser(response.getUsername());
            setJwtCookie(httpResponse, response.getToken());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
            setRefreshCookie(httpResponse, refreshToken.getToken());

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

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletResponse httpResponse) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new AppException("Refresh token invalide", HttpStatus.UNAUTHORIZED));

        User user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateToken(user);
        setJwtCookie(httpResponse, newAccessToken);

        Map<String, String> body = new HashMap<>();
        body.put("username", user.getUsername());
        body.put("role", user.getRole().name());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) RefreshTokenRequest request, HttpServletResponse httpResponse) {
        if (request != null && request.getRefreshToken() != null) {
            refreshTokenService.findByToken(request.getRefreshToken())
                    .ifPresent(rt -> refreshTokenService.deleteByUser(rt.getUser()));
        }

        Cookie jwtCookie = new Cookie("jwt", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        httpResponse.addCookie(jwtCookie);

        httpResponse.addHeader("Set-Cookie",
                "refreshToken=; HttpOnly; Path=/api/auth/refresh; Max-Age=0; SameSite=Strict");

        return ResponseEntity.ok().build();
    }

    private void setJwtCookie(HttpServletResponse response, String token) {
        response.addHeader("Set-Cookie",
                "jwt=" + token + "; HttpOnly; Path=/; Max-Age=86400; SameSite=Strict");
    }

    private void setRefreshCookie(HttpServletResponse response, String token) {
        response.addHeader("Set-Cookie",
                "refreshToken=" + token + "; HttpOnly; Path=/api/auth/refresh; Max-Age=604800; SameSite=Strict");
    }
}
