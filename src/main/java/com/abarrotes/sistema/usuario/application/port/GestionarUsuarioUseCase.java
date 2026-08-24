package com.abarrotes.sistema.usuario.application.port;

import com.abarrotes.sistema.usuario.domain.model.Rol;
import com.abarrotes.sistema.usuario.domain.model.Usuario;

public interface GestionarUsuarioUseCase {
    
    // Este método recibirá los datos base y creará el usuario
    Usuario crearUsuario(String username, String rawPassword, Rol rol);
    
    void desactivarUsuario(Long id);
    
    void activarUsuario(Long id);
}
