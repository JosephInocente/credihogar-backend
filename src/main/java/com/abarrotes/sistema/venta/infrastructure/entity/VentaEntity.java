package com.abarrotes.sistema.venta.infrastructure.entity;

import com.abarrotes.sistema.venta.domain.model.EstadoVenta;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "trabajador_id", nullable = false)
    private Long trabajadorId;

    @Column(name = "viaje_id", nullable = false)
    private Long viajeId;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVenta estado;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    // Relación Uno a Muchos con los detalles de la venta
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleVentaEntity> detalles = new ArrayList<>();

    public void addDetalle(DetalleVentaEntity detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }
}
