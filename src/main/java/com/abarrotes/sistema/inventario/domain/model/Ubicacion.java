package com.abarrotes.sistema.inventario.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ubicacion {
    private Long id;
    private TipoUbicacion tipo;
    private String nombre; // Ej. "Almacén Principal" o "Carro ABC-123"
}