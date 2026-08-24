package com.abarrotes.sistema.venta.infrastructure.rest;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CrearVentaRequest {
    private Long clienteId;
    private Long trabajadorId;
    private Long viajeId;
    private List<DetalleVentaDTO> detalles;
}