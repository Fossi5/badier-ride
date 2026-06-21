package com.badier.badier_ride.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.badier.badier_ride.service.DatabaseUserDetailsService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final DatabaseUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final RateLimitFilter rateLimitFilter;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/add/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/addresses/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/addresses/**").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers(HttpMethod.PUT, "/api/addresses/**").hasAnyRole("ADMIN", "DISPATCHER", "DRIVER")
                        .requestMatchers(HttpMethod.DELETE, "/api/addresses/**").hasAnyRole("ADMIN", "DISPATCHER")

                        .requestMatchers(HttpMethod.GET, "/api/delivery-points/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/delivery-points/**").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers(HttpMethod.PUT, "/api/delivery-points/*/status").hasAnyRole("ADMIN", "DISPATCHER", "DRIVER")
                        .requestMatchers(HttpMethod.PUT, "/api/delivery-points/**").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers(HttpMethod.DELETE, "/api/delivery-points/**").hasAnyRole("ADMIN", "DISPATCHER")

                        .requestMatchers("/api/routes/*/delivery-points/*/proof/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/routes/driver").hasRole("DRIVER")
                        .requestMatchers(HttpMethod.GET, "/api/routes/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/routes/*/messages").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/routes/**").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers(HttpMethod.PUT, "/api/routes/**").hasAnyRole("ADMIN", "DISPATCHER", "DRIVER")
                        .requestMatchers(HttpMethod.DELETE, "/api/routes/**").hasAnyRole("ADMIN", "DISPATCHER")

                        .requestMatchers("/api/notifications/**").authenticated()
.requestMatchers(HttpMethod.GET, "/api/admin/drivers").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers("/api/admin/drivers/available").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers("/api/admin/dispatchers").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/dispatcher/**").hasRole("DISPATCHER")
                        .requestMatchers("/api/driver/**").hasRole("DRIVER")

                        .anyRequest().authenticated())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("Accès non autorisé");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("Accès refusé");
                        }));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3001"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}