package com.abarrotes.sistema.venta.infrastructure.rest;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudStockController {

    private final JdbcTemplate jdbcTemplate;

    @Data
    public static class DetalleSolicitudDTO {
        private Long inventarioId;
        private Integer cantidad;
    }

    @Data
    public static class CrearSolicitudDTO {
        private Long gestorId;
        private String notas;
        private List<DetalleSolicitudDTO> detalles;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> crearSolicitud(@RequestBody CrearSolicitudDTO request) {
        try {
            String sqlSolicitud = "INSERT INTO solicitudes_stock (gestor_id, fecha_hora, estado, notas) VALUES (?, CURRENT_TIMESTAMP, 'PENDIENTE', ?) RETURNING id";
            Long solicitudId = jdbcTemplate.queryForObject(sqlSolicitud, Long.class, request.getGestorId(), request.getNotas());

            String sqlDetalle = "INSERT INTO solicitud_detalles (solicitud_id, inventario_id, cantidad) VALUES (?, ?, ?)";
            for (DetalleSolicitudDTO detalle : request.getDetalles()) {
                jdbcTemplate.update(sqlDetalle, solicitudId, detalle.getInventarioId(), detalle.getCantidad());
            }

            return ResponseEntity.ok(Map.of("mensaje", "Solicitud enviada correctamente", "id", solicitudId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listarSolicitudes(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String rol) {
        try {
            // Se agregó s.motivo_rechazo a la consulta
            String sql = "SELECT s.id, s.fecha_hora, s.estado, s.notas, s.motivo_rechazo, u.username as gestor, " +
                         "(SELECT COUNT(*) FROM solicitud_detalles WHERE solicitud_id = s.id) as total_items " +
                         "FROM solicitudes_stock s " +
                         "JOIN usuarios u ON s.gestor_id = u.id ";
            
            if ("GESTOR".equals(rol) && usuarioId != null) {
                sql += "WHERE s.gestor_id = " + usuarioId + " ";
            }
            sql += "ORDER BY s.fecha_hora DESC";
            
            return ResponseEntity.ok(jdbcTemplate.queryForList(sql));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    @GetMapping("/{id}/detalles")
    public ResponseEntity<?> verDetalles(@PathVariable Long id) {
        try {
            String sql = "SELECT sd.cantidad, p.nombre as producto, pre.nombre as presentacion " +
                         "FROM solicitud_detalles sd " +
                         "JOIN inventario_consolidado ic ON sd.inventario_id = ic.id " +
                         "JOIN presentaciones pre ON ic.presentacion_id = pre.id " +
                         "JOIN productos p ON pre.producto_id = p.id " +
                         "WHERE sd.solicitud_id = ?";
            return ResponseEntity.ok(jdbcTemplate.queryForList(sql, id));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String nuevoEstado = body.get("estado");
            String motivo = body.get("motivo"); // Nuevo campo recibido desde React

            // Si envían un motivo de rechazo, lo guardamos en la base de datos
            if (motivo != null && !motivo.trim().isEmpty()) {
                jdbcTemplate.update("UPDATE solicitudes_stock SET estado = ?, motivo_rechazo = ? WHERE id = ?", nuevoEstado, motivo, id);
            } else {
                jdbcTemplate.update("UPDATE solicitudes_stock SET estado = ? WHERE id = ?", nuevoEstado, id);
            }
            return ResponseEntity.ok(Map.of("mensaje", "Estado actualizado"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}