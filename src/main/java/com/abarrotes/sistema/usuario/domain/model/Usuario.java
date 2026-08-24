package com.abarrotes.sistema.usuario.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    private Long id;
    private String username;
    private String password;
    private Rol rol;
    private EstadoUsuario estado;

    // Reglas de negocio puras (Comportamiento)
    public void desactivar() {
        this.estado = EstadoUsuario.INACTIVO;
    }

    public void activar() {
        this.estado = EstadoUsuario.ACTIVO;
    }

    public boolean isActivo() {
        return this.estado == EstadoUsuario.ACTIVO;
    }
}
