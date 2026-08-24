package com.abarrotes.sistema.auth.infrastructure.rest;

import com.abarrotes.sistema.auth.application.service.AutenticarUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AutenticarUsuarioService autenticarUsuarioService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Delegamos la lógica al servicio
            String token = autenticarUsuarioService.login(request.getUsername(), request.getPassword());
            
            // Retornamos HTTP 200 (OK) con el token en formato JSON
            return ResponseEntity.ok(new AuthResponse(token));
            
        } catch (IllegalArgumentException e) {
            // Si la contraseña está mal o el usuario no existe, retornamos HTTP 401 (No autorizado)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: " + e.getMessage());
        }
    }
}
