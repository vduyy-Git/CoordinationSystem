package com.coordinationsystem.config;

import com.coordinationsystem.dtoresponse.ApiResponse;
import com.coordinationsystem.security.BearerTokenFilter;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.security.cors-origins:http://localhost:5500,http://127.0.0.1:5500,http://localhost:5173,http://127.0.0.1:5173}") String origins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(value -> !value.isBlank()).toList());
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(Arrays.asList("Location"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, BearerTokenFilter bearerTokenFilter,
                                           JsonMapper objectMapper, CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/staff-registration").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                        .requestMatchers("/api/users/**", "/api/roles/**", "/api/user-roles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/donations", "/api/assistance-requests", "/api/donation-items", "/api/request-items").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/donations/*/cancel", "/api/assistance-requests/*/cancel").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/delivery-confirmations").authenticated()
                        .requestMatchers("/api/feedback/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/donations/mine", "/api/assistance-requests/mine", "/api/delivery-confirmations/mine").authenticated()
                        .requestMatchers("/api/profile/**", "/api/service-registrations/**", "/api/dashboard/**", "/api/notifications/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/shipments/mine", "/api/shipments/*/events").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/services").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/items/**", "/api/categories/**", "/api/units/**").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .anyRequest().hasAnyRole("ADMIN", "COORDINATOR"))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(
                                    401, "UNAUTHORIZED", "Cần đăng nhập để sử dụng API", request.getRequestURI(), java.util.List.of()));
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(
                                    403, "FORBIDDEN", "Bạn không có quyền thực hiện thao tác này", request.getRequestURI(), java.util.List.of()));
                        }))
                .addFilterBefore(bearerTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
