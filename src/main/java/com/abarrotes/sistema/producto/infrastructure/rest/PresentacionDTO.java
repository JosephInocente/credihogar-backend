package com.abarrotes.sistema.producto.infrastructure.rest;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class PresentacionDTO {
    private String nombre;
    private String unidadBase;
    private Integer factorConversion;
    private String codigoBarras;
    private BigDecimal precioCompraReferencial;
    private BigDecimal precioVenta;
}