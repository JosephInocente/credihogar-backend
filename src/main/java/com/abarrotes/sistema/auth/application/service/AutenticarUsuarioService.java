package com.abarrotes.sistema.auth.application.service;

import com.abarrotes.sistema.usuario.domain.model.Usuario;
import com.abarrotes.sistema.usuario.domain.port.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutenticarUsuarioService {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String login(String username, String rawPassword) {
        // 1. Buscar al usuario en la base de datos
        Usuario usuario = usuarioRepository.buscarPorUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // 2. Verificar si su estado es ACTIVO
        if (!usuario.isActivo()) {
            throw new IllegalArgumentException("El usuario está inactivo o bloqueado");
        }

        // 3. Comparar la contraseña enviada con la encriptada en la BD
        if (!passwordEncoder.matches(rawPassword, usuario.getPassword())) {
            throw new IllegalArgumentException("Contraseña incorrecta");
        }

        // 4. Si todo es correcto, generamos y devolvemos el token
        return jwtService.generarToken(usuario.getUsername(), usuario.getRol().name());
    }
}