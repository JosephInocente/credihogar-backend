package com.abarrotes.sistema.venta.infrastructure.rest;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class DetalleVentaDTO {
    private Long presentacionId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}