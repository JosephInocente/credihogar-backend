package com.abarrotes.sistema.inventario.infrastructure.entity;

import com.abarrotes.sistema.inventario.domain.model.TipoUbicacion;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ubicaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUbicacion tipo;

    @Column(nullable = false)
    private String nombre;
}