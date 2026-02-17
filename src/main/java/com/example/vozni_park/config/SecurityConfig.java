package com.example.vozni_park.config;

import com.example.vozni_park.security.JwtAccessDeniedHandler;
import com.example.vozni_park.security.JwtAuthenticationEntryPoint;
import com.example.vozni_park.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration with JWT authentication.
 * Protects API endpoints and enables role-based access control.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (not needed for stateless REST APIs with JWT)
                .csrf(csrf -> csrf.disable())

                // Configure CORS
                .cors(cors -> cors.disable()) // For now, allowing all origins via @CrossOrigin

                // Configure exception handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401 Unauthorized
                        .accessDeniedHandler(jwtAccessDeniedHandler)          // 403 Forbidden
                )

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (no authentication required)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/api-docs/**").permitAll()

                        // User Management - Admin only
                        .requestMatchers("/api/users/**").hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")

                        // Vehicle Management - Admin and Driver (read-only for drivers)
                        .requestMatchers(HttpMethod.GET, "/api/vehicles/**").hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN", "DRIVER")
                        .requestMatchers("/api/vehicles/**").hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")

                        // Driver Management - Admin only
                        .requestMatchers("/api/drivers/**").hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")

                        // Travel Orders - All authenticated users can view, only admins can modify
                        .requestMatchers(HttpMethod.GET, "/api/travel-orders/**").hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN", "DRIVER")
                        .requestMatchers("/api/travel-orders/**").hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")

                        // Location Units - Admin only
                        .requestMatchers("/api/locations/**").hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")

                        // Brands, Models, Fuel Types - All authenticated users can view, only admins can modify
                        .requestMatchers(HttpMethod.GET, "/api/brands/**", "/api/vehicle-models/**", "/api/fuel-types/**")
                        .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN", "DRIVER")
                        .requestMatchers("/api/brands/**", "/api/vehicle-models/**", "/api/fuel-types/**")
                        .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")

                        // Roles - Super Admin only
                        .requestMatchers("/api/roles/**").hasRole("SUPER_ADMIN")

                        // Registrations, Licenses, First Aid Kits - Admin only
                        .requestMatchers("/api/registrations/**", "/api/drivers-licenses/**", "/api/first-aid-kits/**")
                        .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Stateless session management (no server-side sessions)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Add JWT filter before Spring Security's default authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Authentication Manager bean for manual authentication
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}