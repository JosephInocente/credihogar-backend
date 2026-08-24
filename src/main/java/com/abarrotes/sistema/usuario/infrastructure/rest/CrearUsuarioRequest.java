package com.abarrotes.sistema.usuario.infrastructure.rest;

import com.abarrotes.sistema.usuario.domain.model.Rol;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearUsuarioRequest {
    private String username;
    private String password;
    private Rol rol;
}