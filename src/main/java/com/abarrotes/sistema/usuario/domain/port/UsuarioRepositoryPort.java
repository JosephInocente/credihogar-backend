package com.abarrotes.sistema.usuario.domain.port;

import com.abarrotes.sistema.usuario.domain.model.Usuario;
import java.util.Optional;

public interface UsuarioRepositoryPort {
    Usuario guardar(Usuario usuario);
    Optional<Usuario> buscarPorUsername(String username);
    boolean existePorUsername(String username);
}
