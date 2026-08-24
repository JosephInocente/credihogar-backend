package com.abarrotes.sistema.usuario.infrastructure.adapter;

import com.abarrotes.sistema.usuario.domain.model.Usuario;
import com.abarrotes.sistema.usuario.domain.port.UsuarioRepositoryPort;
import com.abarrotes.sistema.usuario.infrastructure.entity.UsuarioEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UsuarioJpaAdapter implements UsuarioRepositoryPort {

    private final SpringDataUsuarioRepository repository;

    @Override
    public Usuario guardar(Usuario usuario) {
        // 1. Convertimos el objeto puro del Dominio a una Entidad de Base de Datos
        UsuarioEntity entity = mapearAEntity(usuario);
        
        // 2. Guardamos en PostgreSQL
        UsuarioEntity guardado = repository.save(entity);
        
        // 3. Convertimos la Entidad guardada de vuelta al Dominio
        return mapearADominio(guardado);
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return repository.findByUsername(username)
                .map(this::mapearADominio); // Mapeamos si lo encuentra
    }

    @Override
    public boolean existePorUsername(String username) {
        return repository.existsByUsername(username);
    }

    // --- Métodos Privados de Mapeo ---
    private UsuarioEntity mapearAEntity(Usuario usuario) {
        return UsuarioEntity.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .rol(usuario.getRol())
                .estado(usuario.getEstado())
                .build();
    }

    private Usuario mapearADominio(UsuarioEntity entity) {
        return Usuario.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .password(entity.getPassword())
                .rol(entity.getRol())
                .estado(entity.getEstado())
                .build();
    }
}
