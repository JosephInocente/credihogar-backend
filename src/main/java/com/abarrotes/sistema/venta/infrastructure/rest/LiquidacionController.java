package com.abarrotes.sistema.venta.infrastructure.rest;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/liquidacion")
@RequiredArgsConstructor
public class LiquidacionController {

    private final JdbcTemplate jdbcTemplate;

    @Data
    public static class AjusteStock {
        private Long presentacionId;
        private Integer teorico;
        private Integer fisico;
        private Integer diferencia;
        private String motivo;
    }

    @Data
    public static class CierreViajeRequest {
        private Long viajeId;
        private Long vehiculoId;
        private String accionStock; 
        private List<AjusteStock> ajustes;
    }

    @GetMapping("/resumen/{viajeId}")
    public ResponseEntity<?> obtenerResumenLiquidacion(@PathVariable Long viajeId) {
        try {
            String sqlTotal = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE viaje_id = ? AND estado = 'EMITIDA'";
            Double totalRecaudado = jdbcTemplate.queryForObject(sqlTotal, Double.class, viajeId);

            String sqlDetalle = "SELECT " +
                    "p.nombre AS producto, " +
                    "pre.id AS presentacion_id, " +
                    "pre.nombre AS presentacion, " +
                    "COALESCE((SELECT SUM(vd.cantidad) FROM venta_detalles vd JOIN ventas v ON vd.venta_id = v.id WHERE v.viaje_id = ? AND vd.presentacion_id = pre.id), 0) AS cantidad_vendida, " +
                    "COALESCE(ic.cantidad, 0) AS stock_restante " +
                    "FROM inventario_consolidado ic " +
                    "JOIN presentaciones pre ON ic.presentacion_id = pre.id " +
                    "JOIN productos p ON pre.producto_id = p.id " +
                    "WHERE ic.ubicacion_id = (SELECT vehiculo_id FROM viajes WHERE id = ?) " +
                    "AND (ic.cantidad > 0 OR COALESCE((SELECT SUM(vd.cantidad) FROM venta_detalles vd JOIN ventas v ON vd.venta_id = v.id WHERE v.viaje_id = ? AND vd.presentacion_id = pre.id), 0) > 0)";

            List<Map<String, Object>> detalles = jdbcTemplate.queryForList(sqlDetalle, viajeId, viajeId, viajeId);

            return ResponseEntity.ok(Map.of("totalRecaudado", totalRecaudado, "detalles", detalles));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al calcular liquidación: " + e.getMessage()));
        }
    }

    @PostMapping("/cerrar")
    @Transactional
    public ResponseEntity<?> cerrarViaje(@RequestBody CierreViajeRequest request) {
        try {
            Long vehiculoIdSeguro = jdbcTemplate.queryForObject(
                "SELECT vehiculo_id FROM viajes WHERE id = ?", 
                Long.class, 
                request.getViajeId()
            );

            if (vehiculoIdSeguro != null && vehiculoIdSeguro.equals(1L)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", 
                    "ALERTA CRÍTICA: El vehículo asignado tiene el ID 1. Bloqueo de seguridad activado."
                ));
            }

            // 1. REGISTRAMOS LAS MERMAS/SOBRANTES Y AJUSTAMOS EL INVENTARIO DEL CARRO A LA REALIDAD
            if (request.getAjustes() != null) {
                for (AjusteStock ajuste : request.getAjustes()) {
                    if (ajuste.getDiferencia() != 0) {
                        jdbcTemplate.update("UPDATE inventario_consolidado SET cantidad = cantidad + ? WHERE ubicacion_id = ? AND presentacion_id = ?",
                            ajuste.getDiferencia(), vehiculoIdSeguro, ajuste.getPresentacionId());

                        String tipoMov = ajuste.getDiferencia() < 0 ? "MERMA" : "ENTRADA";
                        String obs = (ajuste.getMotivo() != null && !ajuste.getMotivo().trim().isEmpty()) ? ajuste.getMotivo() : "Ajuste físico en liquidación";
                        
                        jdbcTemplate.update("INSERT INTO movimientos_inventario (ubicacion_origen_id, ubicacion_destino_id, presentacion_id, tipo, cantidad, fecha, usuario_responsable, motivo) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, 'Gerente', ?)",
                            vehiculoIdSeguro, vehiculoIdSeguro, ajuste.getPresentacionId(), tipoMov, Math.abs(ajuste.getDiferencia()), obs);
                    }
                }
            }

            // 2. CERRAMOS EL VIAJE
            jdbcTemplate.update("UPDATE viajes SET estado = 'CERRADO' WHERE id = ?", request.getViajeId());
            jdbcTemplate.update("UPDATE vehiculos SET estado = 'DISPONIBLE' WHERE id = ?", vehiculoIdSeguro);

            // 3. DEVOLVER EL STOCK FÍSICO (YA AJUSTADO) AL ALMACÉN
            if ("DEVOLVER".equalsIgnoreCase(request.getAccionStock())) {
                Long almacenPrincipalId = 1L;

                String sqlRestante = "SELECT presentacion_id, cantidad FROM inventario_consolidado WHERE ubicacion_id = ? AND cantidad > 0";
                List<Map<String, Object>> stockRestante = jdbcTemplate.queryForList(sqlRestante, vehiculoIdSeguro);

                for (Map<String, Object> item : stockRestante) {
                    Long presentacionId = ((Number) item.get("presentacion_id")).longValue();
                    Integer cantidad = ((Number) item.get("cantidad")).intValue();

                    String sqlCheckAlmacen = "SELECT COUNT(*) FROM inventario_consolidado WHERE ubicacion_id = ? AND presentacion_id = ?";
                    Integer existe = jdbcTemplate.queryForObject(sqlCheckAlmacen, Integer.class, almacenPrincipalId, presentacionId);

                    if (existe != null && existe > 0) {
                        jdbcTemplate.update("UPDATE inventario_consolidado SET cantidad = cantidad + ? WHERE ubicacion_id = ? AND presentacion_id = ?", 
                                cantidad, almacenPrincipalId, presentacionId);
                    } else {
                        jdbcTemplate.update("INSERT INTO inventario_consolidado (ubicacion_id, presentacion_id, cantidad) VALUES (?, ?, ?)", 
                                almacenPrincipalId, presentacionId, cantidad);
                    }

                    jdbcTemplate.update("INSERT INTO movimientos_inventario (ubicacion_origen_id, ubicacion_destino_id, presentacion_id, tipo, cantidad, fecha, usuario_responsable, motivo) VALUES (?, ?, ?, 'ENTRADA', ?, CURRENT_TIMESTAMP, 'Sistema', 'Devolución de stock sobrante del viaje')",
                            vehiculoIdSeguro, almacenPrincipalId, presentacionId, cantidad);

                    jdbcTemplate.update("UPDATE inventario_consolidado SET cantidad = 0 WHERE ubicacion_id = ? AND presentacion_id = ?", 
                            vehiculoIdSeguro, presentacionId);
                }
            }

            return ResponseEntity.ok(Map.of("mensaje", "Viaje cerrado y stock cuadrado exitosamente."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al cerrar el viaje: " + e.getMessage()));
        }
    }
}