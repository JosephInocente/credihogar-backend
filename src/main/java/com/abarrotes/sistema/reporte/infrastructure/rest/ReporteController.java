package com.abarrotes.sistema.reporte.infrastructure.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/ventas")
    public ResponseEntity<?> reporte1Ventas() {
        try {
            String sql = "SELECT v.id AS ticket_id, v.fecha_hora, c.razon_social AS cliente, v.total, v.estado " +
                         "FROM ventas v LEFT JOIN clientes c ON v.cliente_id = c.id ORDER BY v.fecha_hora DESC";
            List<Map<String, Object>> lista = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/inventario")
    public ResponseEntity<?> reporte2Inventario() {
        try {
            // CORRECCIÓN ROBUSTA: Buscamos directamente la placa del vehículo según el ubicacion_id
            String sql = "SELECT p.sku, p.nombre AS producto, pre.nombre AS presentacion, " +
                         "CASE " +
                         "  WHEN ic.ubicacion_id = 1 THEN 'Almacén Principal' " +
                         "  ELSE 'Carro ' || COALESCE((SELECT placa FROM vehiculos WHERE id = ic.ubicacion_id), 'Asignado') " +
                         "END AS ubicacion, " +
                         "ic.cantidad " +
                         "FROM inventario_consolidado ic " +
                         "JOIN presentaciones pre ON ic.presentacion_id = pre.id " +
                         "JOIN productos p ON pre.producto_id = p.id " +
                         "WHERE ic.cantidad > 0 " +
                         "ORDER BY ic.ubicacion_id ASC, p.nombre ASC";
                         
            List<Map<String, Object>> lista = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/diferencias")
    public ResponseEntity<?> reporte3Diferencias() {
        try {
            String sql = "SELECT m.id, m.fecha, p.nombre AS producto, m.tipo, m.cantidad, m.motivo, m.usuario_responsable " +
                         "FROM movimientos_inventario m " +
                         "JOIN presentaciones pre ON m.presentacion_id = pre.id " +
                         "JOIN productos p ON pre.producto_id = p.id " +
                         "WHERE m.tipo IN ('MERMA', 'AJUSTE') ORDER BY m.fecha DESC";
            List<Map<String, Object>> lista = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}