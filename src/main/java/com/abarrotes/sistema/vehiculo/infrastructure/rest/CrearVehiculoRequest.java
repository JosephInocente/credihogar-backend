package com.abarrotes.sistema.vehiculo.infrastructure.rest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearVehiculoRequest {
    private String placa;
    private String marca;
    private String modelo;
    private Double capacidad;
    private String observaciones;
}
