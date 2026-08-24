package com.abarrotes.sistema.logistica.infrastructure.rest;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentasController {

    private final JdbcTemplate jdbcTemplate;

    @Data
    public static class ItemVentaDTO {
        private Long presentacionId;
        private Integer cantidad;
        private Double precioBase;
        private Double precioFinal; // El precio negociado por el trabajador
    }

    @Data
    public static class VentaRequestDTO {
        private Long viajeId;
        private Long vehiculoId;
        private Long clienteId; // Puede ser null si es público general
        private Double total;
        private List<ItemVentaDTO> items;
    }

    // 1. Obtener el viaje activo del trabajador logueado (Por ahora le pasamos el ID por URL)
    @GetMapping("/mi-viaje/{trabajadorId}")
    public ResponseEntity<?> obtenerMiViajeActivo(@PathVariable Long trabajadorId) {
        try {
            String sql = "SELECT v.id, v.vehiculo_id, veh.placa, v.destino, v.estado " +
                         "FROM viajes v " +
                         "JOIN vehiculos veh ON v.vehiculo_id = veh.id " +
                         "WHERE v.trabajador_id = ? AND v.estado IN ('CARGADO', 'EN_RUTA') " +
                         "ORDER BY v.fecha DESC LIMIT 1";
            Map<String, Object> viaje = jdbcTemplate.queryForMap(sql, trabajadorId);
            return ResponseEntity.ok(viaje);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return ResponseEntity.ok(Map.of("mensaje", "No tienes viajes activos en ruta."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // 2. Obtener el inventario físico que está DENTRO del camión
    @GetMapping("/mi-inventario/{vehiculoId}")
    public ResponseEntity<?> obtenerInventarioVehiculo(@PathVariable Long vehiculoId) {
        try {
            String sql = "SELECT ic.presentacion_id, p.nombre AS producto, pre.nombre AS presentacion, ic.cantidad, pre.precio_venta AS precio_base " +
                         "FROM inventario_consolidado ic " +
                         "JOIN presentaciones pre ON ic.presentacion_id = pre.id " +
                         "JOIN productos p ON pre.producto_id = p.id " +
                         "WHERE ic.ubicacion_id = ? AND ic.cantidad > 0";
            return ResponseEntity.ok(jdbcTemplate.queryForList(sql, vehiculoId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    // 3. Registrar la venta en la calle
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarVenta(@RequestBody VentaRequestDTO request) {
        try {
            // A. Crear la cabecera de la venta
            String sqlVenta = "INSERT INTO ventas (viaje_id, cliente_id, total, fecha) VALUES (?, ?, ?, CURRENT_TIMESTAMP) RETURNING id";
            Long ventaId = jdbcTemplate.queryForObject(sqlVenta, Long.class, 
                request.getViajeId(), request.getClienteId(), request.getTotal());

            // B. Procesar cada producto vendido
            for (ItemVentaDTO item : request.getItems()) {
                // 1. Guardar el detalle de la venta (Usamos precio_snapshot según tu documento para guardar a cuánto se vendió realmente)
                jdbcTemplate.update("INSERT INTO venta_detalles (venta_id, presentacion_id, cantidad, precio_snapshot) VALUES (?, ?, ?, ?)",
                    ventaId, item.getPresentacionId(), item.getCantidad(), item.getPrecioFinal());

                // 2. Descontar el stock DEL CAMIÓN (ubicacion_id = vehiculoId)
                jdbcTemplate.update("UPDATE inventario_consolidado SET cantidad = cantidad - ? WHERE ubicacion_id = ? AND presentacion_id = ?",
                    item.getCantidad(), request.getVehiculoId(), item.getPresentacionId());
            }

            return ResponseEntity.ok(Map.of("mensaje", "Venta registrada exitosamente", "ventaId", ventaId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al registrar venta: " + e.getMessage()));
        }
    }
}
