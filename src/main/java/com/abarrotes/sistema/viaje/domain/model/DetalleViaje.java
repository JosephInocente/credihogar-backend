package com.abarrotes.sistema.viaje.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleViaje {
    private Long id;
    private Long presentacionId;
    private Integer cantidad;
}