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

import java.util.List;

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
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(request -> {
                    var config = new org.springframework.web.cors.CorsConfiguration();
                    config.setAllowedOrigins(List.of(
                            // Production
                            "https://vozni-park-frontend.azurewebsites.net",
                            // Local development — Vite dev server and Docker Nginx
                            "http://localhost",
                            "http://localhost:5173",
                            "http://localhost:80"
                    ));
                    config.setAllowedMethods(List.of(
                            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(false);
                    return config;
                }))

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                .authorizeHttpRequests(auth -> auth

                        // ── Public ────────────────────────────────────────────────
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/benchmark/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/api-docs/**").permitAll()

                        // ── User management ───────────────────────────────────────
                        .requestMatchers("/api/users/**")
                                .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")

                        // ── Vehicles ──────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/vehicles/**")
                                .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")
                        .requestMatchers("/api/vehicles/**")
                                .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")

                        // ── Vehicle faults ────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/vehicle-faults/**")
                                .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")
                        .requestMatchers("/api/vehicle-faults/**")
                                .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")

                        // ── Traffic accidents ─────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/traffic-accidents/**")
                                .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")
                        .requestMatchers("/api/traffic-accidents/**")
                                .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")

                        // ── Vehicle assigned drivers ──────────────────────────────
                        .requestMatchers("/api/vehicle-assigned-drivers/**")
                                .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")

                        // ── Vehicle documents ─────────────────────────────────────
                        .requestMatchers("/api/vehicle-documents/**")
                                .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")

                        // ── Drivers ───────────────────────────────────────────────
                        .requestMatchers("/api/drivers/**")
                                .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")

                        // ── Travel orders ─────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/travel-orders/**")
                                .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")
                        .requestMatchers("/api/travel-orders/**")
                                .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")

                        // ── Locations ─────────────────────────────────────────────
                        .requestMatchers("/api/locations/**")
                                .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")

                        // ── Reference data (brands, models, fuel types, types) ────
                        .requestMatchers(HttpMethod.GET,
                                "/api/brands/**", "/api/vehicle-models/**",
                                "/api/fuel-types/**", "/api/vehicle-types/**",
                                "/api/travel-order-types/**")
                                .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")
                        .requestMatchers(
                                "/api/brands/**", "/api/vehicle-models/**",
                                "/api/fuel-types/**", "/api/vehicle-types/**",
                                "/api/travel-order-types/**")
                                .hasRole("SUPER_ADMIN")

                        // ── Roles ─────────────────────────────────────────────────
                        .requestMatchers("/api/roles/**")
                                .hasRole("SUPER_ADMIN")

                        // ── Document entities (registrations, licenses, kits) ─────
                        .requestMatchers(
                                "/api/registrations/**",
                                "/api/drivers-licenses/**",
                                "/api/first-aid-kits/**",
                                "/api/casco-insurances/**",
                                "/api/tachograph-devices/**",
                                "/api/fire-extinguishers/**",
                                "/api/medical-certificates/**",
                                "/api/tachograph-cards/**",
                                "/api/driver-permits/**")
                                .hasAnyRole("SUPER_ADMIN", "LOCAL_ADMIN")

                        // ── Admin refresh endpoint ────────────────────────────────
                        .requestMatchers("/api/admin/**")
                                .hasRole("SUPER_ADMIN")

                        // ── Everything else requires authentication ───────────────
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
