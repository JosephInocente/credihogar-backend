package com.abarrotes.sistema.reporte.infrastructure.rest;

import com.abarrotes.sistema.reporte.application.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    // 1. Inyectamos el servicio que tú ya tenías
    private final DashboardService dashboardService;
    
    // 2. Inyectamos JdbcTemplate para nuestras nuevas métricas de logística
    private final JdbcTemplate jdbcTemplate;

    // --- TU ENDPOINT ORIGINAL (No lo tocamos) ---
    @GetMapping
    public ResponseEntity<?> obtenerDashboard() {
        // Retornamos la respuesta directamente desde PostgreSQL usando tu servicio original
        return ResponseEntity.ok(dashboardService.obtenerDatosReales());
    }

    // --- NUESTRO NUEVO ENDPOINT PARA LA PANTALLA DEL GERENTE ---
    @GetMapping("/metricas")
    public ResponseEntity<?> obtenerMetricas() {
        try {
            // 1. Ventas del Día (Solo ventas confirmadas/emitidas hoy)
            String sqlVentasDia = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE DATE(fecha_hora) = CURRENT_DATE AND estado = 'EMITIDA'";
            Double ventasDia = jdbcTemplate.queryForObject(sqlVentasDia, Double.class);

            // 2. Ventas del Mes
            String sqlVentasMes = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE EXTRACT(MONTH FROM fecha_hora) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM fecha_hora) = EXTRACT(YEAR FROM CURRENT_DATE) AND estado = 'EMITIDA'";
            Double ventasMes = jdbcTemplate.queryForObject(sqlVentasMes, Double.class);

            // 3. Carros en Ruta
            String sqlCarrosEnRuta = "SELECT COUNT(*) FROM vehiculos WHERE estado = 'EN_RUTA'";
            Integer carrosEnRuta = jdbcTemplate.queryForObject(sqlCarrosEnRuta, Integer.class);

            // 4. Viajes Pendientes de Liquidación (Cargados o en ruta)
            String sqlViajesPendientes = "SELECT COUNT(*) FROM viajes WHERE estado IN ('CARGADO', 'EN_RUTA')";
            Integer viajesPendientes = jdbcTemplate.queryForObject(sqlViajesPendientes, Integer.class);

            // 5. Alertas de Stock (Productos en el Almacén Principal con menos de 10 unidades)
            String sqlAlertasStock = "SELECT p.nombre AS producto, pre.nombre AS presentacion, ic.cantidad " +
                    "FROM inventario_consolidado ic " +
                    "JOIN presentaciones pre ON ic.presentacion_id = pre.id " +
                    "JOIN productos p ON pre.producto_id = p.id " +
                    "WHERE ic.ubicacion_id = 1 AND ic.cantidad <= 10 " +
                    "ORDER BY ic.cantidad ASC LIMIT 5";
            List<Map<String, Object>> alertasStock = jdbcTemplate.queryForList(sqlAlertasStock);

            return ResponseEntity.ok(Map.of(
                    "ventasDia", ventasDia,
                    "ventasMes", ventasMes,
                    "carrosEnRuta", carrosEnRuta,
                    "viajesPendientes", viajesPendientes,
                    "alertasStock", alertasStock
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al cargar métricas del dashboard: " + e.getMessage()));
        }
    }
}