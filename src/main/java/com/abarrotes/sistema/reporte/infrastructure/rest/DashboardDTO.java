package com.abarrotes.sistema.reporte.infrastructure.rest;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class DashboardDTO {
    private BigDecimal ventasDelDia;
    private BigDecimal ventasDelMes;
    private Long carrosEnRuta;
    private Long viajesPendientesLiquidacion;
    private Long productosConBajoStock;
}
