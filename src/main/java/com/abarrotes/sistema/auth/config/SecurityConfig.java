package com.abarrotes.sistema.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults()) 
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() 
                .requestMatchers("/api/auth/**").permitAll() 
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // Agrupamos la ruta completa de usuarios para el GERENTE
                .requestMatchers("/api/usuarios", "/api/usuarios/**").hasRole("GERENTE")
                
                .requestMatchers("/api/productos/**").hasRole("GERENTE")
                .requestMatchers("/api/inventario/**").hasRole("GERENTE")
                .requestMatchers("/api/vehiculos/**").hasRole("GERENTE")
                .requestMatchers("/api/viajes/**").hasRole("GERENTE")
                .requestMatchers("/api/dashboard/**").hasRole("GERENTE")
                .requestMatchers("/api/logistica/**").hasRole("GERENTE") 
                
                .requestMatchers("/api/clientes/**").hasAnyRole("GERENTE", "TRABAJADOR")
                .requestMatchers("/api/ventas/**").hasAnyRole("GERENTE", "TRABAJADOR")
                .requestMatchers("/api/pos/**").hasAnyRole("GERENTE", "TRABAJADOR")
                
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}