package com.abarrotes.sistema.viaje.infrastructure.rest;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AgregarCargaRequest {
    private List<DetalleViajeDTO> detalles;
}