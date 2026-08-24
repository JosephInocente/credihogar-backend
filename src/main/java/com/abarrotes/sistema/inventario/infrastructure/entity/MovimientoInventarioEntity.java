package com.abarrotes.sistema.inventario.infrastructure.entity;

import com.abarrotes.sistema.inventario.domain.model.TipoMovimiento;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_inventario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoInventarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ubicacion_origen_id")
    private Long ubicacionOrigenId; // Puede ser null en ENTRADAS

    @Column(name = "ubicacion_destino_id")
    private Long ubicacionDestinoId;

    @Column(name = "presentacion_id", nullable = false)
    private Long presentacionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "usuario_responsable", nullable = false)
    private String usuarioResponsable;

    private String motivo;
}
