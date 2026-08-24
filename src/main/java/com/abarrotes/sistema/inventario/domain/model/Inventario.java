package com.abarrotes.sistema.inventario.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventario {
    private Long id;
    private Long ubicacionId;
    private Long presentacionId;
    private Integer cantidad; // Siempre no negativo (RF29)

    public void sumar(Integer cantidadASumar) {
        this.cantidad += cantidadASumar;
    }

    public void restar(Integer cantidadARestar) {
        if (this.cantidad < cantidadARestar) {
            throw new IllegalArgumentException("Stock insuficiente en la ubicación");
        }
        this.cantidad -= cantidadARestar;
    }
}