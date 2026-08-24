package com.abarrotes.sistema.inventario.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventario_consolidado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ubicacion_id", nullable = false)
    private Long ubicacionId;

    @Column(name = "presentacion_id", nullable = false)
    private Long presentacionId;

    @Column(nullable = false)
    private Integer cantidad;
}