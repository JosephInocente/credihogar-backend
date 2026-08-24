package com.abarrotes.sistema.producto.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "presentaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresentacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación Muchos a Uno: Muchas presentaciones pertenecen a un producto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private ProductoEntity producto;

    @Column(nullable = false)
    private String nombre; // Ej. "Caja x 12 botellas"

    @Column(name = "unidad_base", nullable = false)
    private String unidadBase; 

    @Column(name = "factor_conversion", nullable = false)
    private Integer factorConversion; 

    @Column(name = "codigo_barras")
    private String codigoBarras;

    @Column(name = "precio_compra_referencial", precision = 10, scale = 2)
    private BigDecimal precioCompraReferencial;

    @Column(name = "precio_venta", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioVenta;
}
