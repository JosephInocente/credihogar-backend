package com.abarrotes.sistema.producto.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Presentacion {
    private Long id;
    private Long productoId; // Referencia al producto padre
    private String nombre; // Ej. "Caja x 12 botellas"
    private String unidadBase; // Ej. "Botella"
    private Integer factorConversion; // Ej. 12
    private String codigoBarras;
    private BigDecimal precioCompraReferencial;
    private BigDecimal precioVenta; // Precio autorizado para el trabajador
}