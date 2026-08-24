package com.abarrotes.sistema.usuario.infrastructure.adapter;

import com.abarrotes.sistema.usuario.infrastructure.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    
    // Spring crea automáticamente la consulta SQL por el nombre del método
    Optional<UsuarioEntity> findByUsername(String username);
    
    boolean existsByUsername(String username);
}