package com.abarrotes.sistema.cliente.infrastructure.adapter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RucResponseDTO {
    private String numeroDocumento;
    private String razonSocial;
    private String estado;
    private String direccion;
}