package com.abarrotes.sistema.viaje.infrastructure.entity;

import com.abarrotes.sistema.viaje.domain.model.EstadoViaje;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "viajes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViajeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private String destino;

    @Column(name = "almacen_origen_id", nullable = false)
    private Long almacenOrigenId;

    @Column(name = "vehiculo_id", nullable = false)
    private Long vehiculoId;

    @Column(name = "trabajador_id", nullable = false)
    private Long trabajadorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoViaje estado;
    
 // NUEVO: Relación Uno a Muchos
    @OneToMany(mappedBy = "viaje", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleViajeEntity> detalles = new ArrayList<>();

    // Método utilitario para mantener la relación sincronizada
    public void addDetalle(DetalleViajeEntity detalle) {
        detalles.add(detalle);
        detalle.setViaje(this);
    }
}