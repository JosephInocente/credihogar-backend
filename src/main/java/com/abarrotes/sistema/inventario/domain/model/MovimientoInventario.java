package com.abarrotes.sistema.inventario.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoInventario {
    private Long id;
    private Long ubicacionOrigenId; // Puede ser null si es una ENTRADA nueva
    private Long ubicacionDestinoId;
    private Long presentacionId;
    private TipoMovimiento tipo;
    private Integer cantidad;
    private LocalDateTime fecha;
    private String usuarioResponsable;
    private String motivo; // Justificación (RF26)
}