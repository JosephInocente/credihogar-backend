package com.abarrotes.sistema.usuario.application.service;

import com.abarrotes.sistema.usuario.application.port.GestionarUsuarioUseCase;
import com.abarrotes.sistema.usuario.domain.model.EstadoUsuario;
import com.abarrotes.sistema.usuario.domain.model.Rol;
import com.abarrotes.sistema.usuario.domain.model.Usuario;
import com.abarrotes.sistema.usuario.domain.port.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GestionarUsuarioService implements GestionarUsuarioUseCase {

    // Inyectamos el puerto de salida y el codificador de contraseñas
    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Usuario crearUsuario(String username, String rawPassword, Rol rol) {
        // 1. Validar que el usuario no exista
        if (usuarioRepository.existePorUsername(username)) {
            // Lógica de error, más adelante crearemos excepciones personalizadas
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }

        // 2. Encriptar la contraseña (RNF19: Argon2 / bcrypt)
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 3. Crear el objeto de dominio con el Builder
        Usuario nuevoUsuario = Usuario.builder()
                .username(username)
                .password(encodedPassword)
                .rol(rol)
                .estado(EstadoUsuario.ACTIVO) // Estado inicial por defecto
                .build();

        // 4. Guardar usando el puerto de salida
        return usuarioRepository.guardar(nuevoUsuario);
    }

    @Override
    public void desactivarUsuario(Long id) {
        // En una implementación real, buscaríamos primero al usuario por ID
        // Para este paso inicial, dejaremos la estructura lista.
        throw new UnsupportedOperationException("Método pendiente de implementar");
    }

    @Override
    public void activarUsuario(Long id) {
        throw new UnsupportedOperationException("Método pendiente de implementar");
    }
}
