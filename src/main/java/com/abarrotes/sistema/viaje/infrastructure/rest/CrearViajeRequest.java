package com.abarrotes.sistema.viaje.infrastructure.rest;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class CrearViajeRequest {
    private LocalDate fecha; // Spring Boot convertirá automáticamente formatos como "2026-08-15"
    private String destino;
    private Long almacenOrigenId;
    private Long vehiculoId;
    private Long trabajadorId;
}