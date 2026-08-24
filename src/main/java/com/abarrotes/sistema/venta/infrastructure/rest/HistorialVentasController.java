package com.abarrotes.sistema.venta.infrastructure.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historial-ventas")
@RequiredArgsConstructor
public class HistorialVentasController {

    private final JdbcTemplate jdbcTemplate;

    // 1. Obtener la lista completa de ventas con datos del cliente y el viaje
    @GetMapping
    public ResponseEntity<?> listarHistorialClientesYVentas() {
        try {
            String sql = "SELECT " +
                    "v.id AS venta_id, " +
                    "c.tipo_documento, " +
                    "c.numero_documento, " +
                    "c.razon_social AS cliente_nombre, " +
                    "vj.destino AS direccion_viaje, " +
                    "TO_CHAR(v.fecha_hora, 'DD/MM/YYYY HH:MI:SS AM') as fecha_hora, " +
                    "v.total " +
                    "FROM ventas v " +
                    "JOIN clientes c ON v.cliente_id = c.id " +
                    "JOIN viajes vj ON v.viaje_id = vj.id " +
                    "ORDER BY v.fecha_hora DESC";
            
            return ResponseEntity.ok(jdbcTemplate.queryForList(sql));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    // 2. Obtener los detalles específicos de un ticket
    @GetMapping("/{ventaId}/ticket")
    public ResponseEntity<?> obtenerDetalleTicket(@PathVariable Long ventaId) {
        try {
            String sql = "SELECT " +
                    "vd.cantidad, " +
                    "p.nombre AS producto, " +
                    "pre.nombre AS presentacion, " +
                    "vd.precio_unitario, " +
                    "(vd.cantidad * vd.precio_unitario) AS subtotal " +
                    "FROM venta_detalles vd " +
                    "JOIN presentaciones pre ON vd.presentacion_id = pre.id " +
                    "JOIN productos p ON pre.producto_id = p.id " +
                    "WHERE vd.venta_id = ?";
            
            return ResponseEntity.ok(jdbcTemplate.queryForList(sql, ventaId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of());
        }
    }
}