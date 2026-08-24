package com.abarrotes.sistema.reporte.application.service;

import com.abarrotes.sistema.reporte.infrastructure.rest.DashboardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;

    public DashboardDTO obtenerDatosReales() {
        
        // 1. Calcular Ventas del Día
        // ¡CAMBIA 'fecha_emision' POR EL NOMBRE REAL DE TU COLUMNA SI ES DIFERENTE!
        String sqlVentasDia = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE DATE(fecha_hora) = CURRENT_DATE";
        BigDecimal ventasDia = jdbcTemplate.queryForObject(sqlVentasDia, BigDecimal.class);

        // 2. Calcular Ventas del Mes
        // ¡CAMBIA 'fecha_emision' AQUÍ TAMBIÉN!
        String sqlVentasMes = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE EXTRACT(MONTH FROM fecha_hora) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM fecha_hora) = EXTRACT(YEAR FROM CURRENT_DATE)";
        BigDecimal ventasMes = jdbcTemplate.queryForObject(sqlVentasMes, BigDecimal.class);

        // 3. Contar Carros en Ruta
        String sqlCarrosRuta = "SELECT COUNT(*) FROM viajes WHERE estado = 'EN_RUTA'";
        Long carrosRuta = jdbcTemplate.queryForObject(sqlCarrosRuta, Long.class);

        // 4. Contar Viajes pendientes de liquidar
        String sqlViajesPendientes = "SELECT COUNT(*) FROM viajes WHERE estado IN ('CARGADO', 'RETORNADO', 'EN_LIQUIDACION')";
        Long viajesPendientes = jdbcTemplate.queryForObject(sqlViajesPendientes, Long.class);

        // 5. Contar Productos con Bajo Stock
        String sqlBajoStock = "SELECT COUNT(*) FROM inventario_consolidado WHERE cantidad <= 15";
        Long bajoStock = jdbcTemplate.queryForObject(sqlBajoStock, Long.class);

        return DashboardDTO.builder()
                .ventasDelDia(ventasDia)
                .ventasDelMes(ventasMes)
                .carrosEnRuta(carrosRuta)
                .viajesPendientesLiquidacion(viajesPendientes)
                .productosConBajoStock(bajoStock)
                .build();
    }
}