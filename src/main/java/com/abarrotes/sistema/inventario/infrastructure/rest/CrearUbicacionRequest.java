package com.abarrotes.sistema.inventario.infrastructure.rest;

import com.abarrotes.sistema.inventario.domain.model.TipoUbicacion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearUbicacionRequest {
    private String nombre;
    private TipoUbicacion tipo;
}
