package com.abarrotes.sistema.cliente.infrastructure.rest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearClienteRequest {
    private String tipoDocumento;
    private String numeroDocumento;
    private String razonSocial;
    private String direccion;
    private String telefono;
}
