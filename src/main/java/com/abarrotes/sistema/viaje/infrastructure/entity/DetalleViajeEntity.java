package com.abarrotes.sistema.viaje.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "viaje_detalles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleViajeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación Muchos a Uno con el Viaje
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false)
    private ViajeEntity viaje;

    @Column(name = "presentacion_id", nullable = false)
    private Long presentacionId;

    @Column(nullable = false)
    private Integer cantidad;
}