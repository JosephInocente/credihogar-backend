package com.abarrotes.sistema.vehiculo.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehiculo {
    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private Double capacidad; // Puede representar toneladas o volumen
    private EstadoVehiculo estado;
    private String observaciones;

    // Regla de negocio: Un vehículo en mantenimiento o inactivo no puede asignarse (RF37 / HU-09)
    public boolean isDisponibleParaViaje() {
        return this.estado == EstadoVehiculo.DISPONIBLE;
    }

    public void marcarComoAsignado() {
        if (!isDisponibleParaViaje()) {
            throw new IllegalStateException("El vehículo no está disponible para ser asignado");
        }
        this.estado = EstadoVehiculo.ASIGNADO;
    }
}
