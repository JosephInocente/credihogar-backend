package com.abarrotes.sistema.inventario.infrastructure.rest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrarEntradaRequest {
    private Long ubicacionDestinoId;
    private Long presentacionId;
    private Integer cantidad;
    private String motivo;
    private String documentoReferencia; // Agregado para cumplir con el RF26
}