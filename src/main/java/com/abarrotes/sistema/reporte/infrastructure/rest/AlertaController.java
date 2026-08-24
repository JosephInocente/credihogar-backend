package com.abarrotes.sistema.reporte.infrastructure.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/stock-bajo")
    public ResponseEntity<?> listarStockBajo() {
        try {
            // Filtra productos cuyo stock consolidado sea menor o igual a 5 unidades (umbral crítico)
            String sql = "SELECT p.sku, p.nombre AS producto, pre.nombre AS presentacion, " +
                         "CASE WHEN u.tipo = 'ALMACEN' THEN 'Almacén Principal' " +
                         "ELSE 'Carro en Ruta' END AS ubicacion, " +
                         "ic.cantidad AS stock_actual " +
                         "FROM inventario_consolidado ic " +
                         "JOIN presentaciones pre ON ic.presentacion_id = pre.id " +
                         "JOIN productos p ON pre.producto_id = p.id " +
                         "JOIN ubicaciones u ON ic.ubicacion_id = u.id " +
                         "WHERE ic.cantidad <= 10 " +
                         "ORDER BY ic.cantidad ASC";
                         
            List<Map<String, Object>> lista = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}