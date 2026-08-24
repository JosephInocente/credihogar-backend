package com.abarrotes.sistema.viaje.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Viaje {
    private Long id;
    private LocalDate fecha;
    private String destino;
    private Long almacenOrigenId;
    private Long vehiculoId;
    private Long trabajadorId;
    private EstadoViaje estado;

    // NUEVO: Lista de carga (Detalles)
    @Builder.Default
    private List<DetalleViaje> detalles = new ArrayList<>();

    public void agregarDetalle(DetalleViaje detalle) {
        this.detalles.add(detalle);
    }

    public void iniciarPreparacionCarga() {
        if (this.estado != EstadoViaje.BORRADOR) {
            throw new IllegalStateException("Solo un viaje en BORRADOR puede pasar a PREPARANDO_CARGA");
        }
        this.estado = EstadoViaje.PREPARANDO_CARGA;
    }
}