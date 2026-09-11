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

    // Obtener la lista completa o filtrada por el rol del usuario
    @GetMapping
    public ResponseEntity<?> listarHistorialClientesYVentas(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String rol) {
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
                    "LEFT JOIN clientes c ON v.cliente_id = c.id " +
                    "LEFT JOIN viajes vj ON v.viaje_id = vj.id ";

            // Si es GESTOR, concatenamos la cláusula WHERE para filtrar solo sus ventas
            if ("GESTOR".equals(rol) && usuarioId != null) {
                sql += "WHERE v.trabajador_id = " + usuarioId + " ";
            }
            
            sql += "ORDER BY v.fecha_hora DESC";
            
            return ResponseEntity.ok(jdbcTemplate.queryForList(sql));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    // Obtener los detalles específicos de un ticket
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