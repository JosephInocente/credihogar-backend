package com.abarrotes.sistema.logistica.infrastructure.rest;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logistica")
@RequiredArgsConstructor
public class LogisticaController {

    private final JdbcTemplate jdbcTemplate;

    // --- DTOs ---
    @Data
    public static class VehiculoDTO {
        private Long id;
        private String placa;
        private String marca;
        private String modelo;
        private Double capacidad;
        private String estado;
        private String observaciones;
    }

    @Data
    public static class ViajeDTO {
        private Long id;
        private String destino;
        private String estado;
        private String fecha;
        private String vehiculoPlaca;
        private Long trabajadorId;
    }
    
    @Data
    public static class ViajeRequestDTO {
        private String destino;
        private Long vehiculoId;
        private Long trabajadorId;
        private String fecha; 
    }

    @Data
    public static class DetalleCargaDTO {
        private Long presentacionId;
        private Integer cantidad;
        private Double precioVenta; 
    }

    @Data
    public static class ConfirmarCargaRequestDTO {
        private List<DetalleCargaDTO> items;
    }

    // --- ENDPOINTS VEHÍCULOS ---
    @GetMapping("/vehiculos")
    public ResponseEntity<List<VehiculoDTO>> obtenerVehiculos() {
        String sql = "SELECT id, placa, marca, modelo, capacidad, estado, observaciones FROM vehiculos ORDER BY id ASC";
        List<VehiculoDTO> lista = jdbcTemplate.query(sql, (rs, rowNum) -> {
            VehiculoDTO dto = new VehiculoDTO();
            dto.setId(rs.getLong("id"));
            dto.setPlaca(rs.getString("placa"));
            dto.setMarca(rs.getString("marca"));
            dto.setModelo(rs.getString("modelo"));
            dto.setCapacidad(rs.getDouble("capacidad"));
            dto.setEstado(rs.getString("estado"));
            dto.setObservaciones(rs.getString("observaciones"));
            return dto;
        });
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/vehiculos")
    public ResponseEntity<?> registrarVehiculo(@RequestBody VehiculoDTO request) {
        try {
            String sql = "INSERT INTO vehiculos (placa, marca, modelo, capacidad, estado, observaciones) VALUES (?, ?, ?, ?, 'DISPONIBLE', ?) RETURNING id";
            Long id = jdbcTemplate.queryForObject(sql, Long.class, 
                request.getPlaca(), 
                request.getMarca(), 
                request.getModelo(), 
                request.getCapacidad(), 
                request.getObservaciones() != null ? request.getObservaciones() : ""); // Protección anti-nulos
            return ResponseEntity.ok(Map.of("mensaje", "Vehículo registrado exitosamente", "id", id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al registrar vehículo: " + e.getMessage()));
        }
    }
    
    @PutMapping("/vehiculos/{id}")
    public ResponseEntity<?> actualizarVehiculo(@PathVariable Long id, @RequestBody VehiculoDTO request) {
        try {
            String sql = "UPDATE vehiculos SET placa = ?, marca = ?, modelo = ?, capacidad = ?, estado = ?, observaciones = ? WHERE id = ?";
            jdbcTemplate.update(sql, 
                request.getPlaca(), 
                request.getMarca(), 
                request.getModelo(), 
                request.getCapacidad(),
                request.getEstado() != null ? request.getEstado() : "DISPONIBLE", 
                request.getObservaciones() != null ? request.getObservaciones() : "", // Protección anti-nulos
                id);
            return ResponseEntity.ok(Map.of("mensaje", "Vehículo actualizado exitosamente"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al actualizar vehículo: " + e.getMessage()));
        }
    }

    @DeleteMapping("/vehiculos/{id}")
    public ResponseEntity<?> eliminarVehiculo(@PathVariable Long id) {
        try {
            Integer viajesCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM viajes WHERE vehiculo_id = ?", Integer.class, id);
            if (viajesCount != null && viajesCount > 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "No se puede eliminar el vehículo porque tiene viajes registrados en su historial."));
            }
            jdbcTemplate.update("DELETE FROM vehiculos WHERE id = ?", id);
            return ResponseEntity.ok(Map.of("mensaje", "Vehículo eliminado exitosamente"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al eliminar vehículo: " + e.getMessage()));
        }
    }

    // --- ENDPOINTS VIAJES ---
    @GetMapping("/viajes")
    public ResponseEntity<List<ViajeDTO>> obtenerViajes() {
        String sql = "SELECT v.id, v.destino, v.estado, v.fecha, veh.placa AS vehiculo_placa, v.trabajador_id " +
                     "FROM viajes v JOIN vehiculos veh ON v.vehiculo_id = veh.id ORDER BY v.fecha DESC";
        List<ViajeDTO> lista = jdbcTemplate.query(sql, (rs, rowNum) -> {
            ViajeDTO dto = new ViajeDTO();
            dto.setId(rs.getLong("id"));
            dto.setDestino(rs.getString("destino"));
            dto.setEstado(rs.getString("estado"));
            if(rs.getDate("fecha") != null) { dto.setFecha(rs.getDate("fecha").toString()); }
            dto.setVehiculoPlaca(rs.getString("vehiculo_placa"));
            dto.setTrabajadorId(rs.getLong("trabajador_id"));
            return dto;
        });
        return ResponseEntity.ok(lista);
    }
    
    @PostMapping("/viajes")
    public ResponseEntity<?> programarViaje(@RequestBody ViajeRequestDTO request) {
        try {
            Long almacenOrigenId = 1L; 
            String estado = "BORRADOR"; 
            String sql = "INSERT INTO viajes (almacen_origen_id, destino, estado, fecha, trabajador_id, vehiculo_id) VALUES (?, ?, ?, ?::date, ?, ?) RETURNING id";
            Long id = jdbcTemplate.queryForObject(sql, Long.class, almacenOrigenId, request.getDestino(), estado,
                request.getFecha(), request.getTrabajadorId(), request.getVehiculoId());
            return ResponseEntity.ok(Map.of("mensaje", "Viaje programado exitosamente", "id", id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al programar viaje: " + e.getMessage()));
        }
    }

    // --- ENDPOINTS DE CARGA DEL VIAJE ---
    @GetMapping("/viajes/{viajeId}/carga")
    public ResponseEntity<?> obtenerDetalleCarga(@PathVariable Long viajeId) {
        try {
            String sqlDetalles = "SELECT vd.cantidad, pre.precio_venta, pre.nombre AS presentacion_nombre, p.nombre AS producto_nombre " +
                                 "FROM viaje_detalles vd " +
                                 "JOIN presentaciones pre ON vd.presentacion_id = pre.id " +
                                 "JOIN productos p ON pre.producto_id = p.id " +
                                 "WHERE vd.viaje_id = ?";
                                 
            List<Map<String, Object>> detalles = jdbcTemplate.queryForList(sqlDetalles, viajeId);
            return ResponseEntity.ok(Map.of("viajeId", viajeId, "items", detalles));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error obteniendo la carga: " + e.getMessage()));
        }
    }

    @PostMapping("/viajes/{viajeId}/carga")
    public ResponseEntity<?> confirmarCargaViaje(@PathVariable Long viajeId, @RequestBody ConfirmarCargaRequestDTO request) {
        try {
            Map<String, Object> viaje = jdbcTemplate.queryForMap("SELECT vehiculo_id, estado FROM viajes WHERE id = ?", viajeId);
            if (!"BORRADOR".equals(viaje.get("estado"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Solo se puede cargar un viaje en estado BORRADOR."));
            }

            Long vehiculoId = ((Number) viaje.get("vehiculo_id")).longValue();

            for (DetalleCargaDTO item : request.getItems()) {
                jdbcTemplate.update("INSERT INTO viaje_detalles (viaje_id, presentacion_id, cantidad) VALUES (?, ?, ?)",
                        viajeId, item.getPresentacionId(), item.getCantidad());

                // Descontar del Almacén Principal
                jdbcTemplate.update("UPDATE inventario_consolidado SET cantidad = cantidad - ? WHERE ubicacion_id = 1 AND presentacion_id = ?",
                        item.getCantidad(), item.getPresentacionId());

                // Sumar al inventario del Vehículo
                String upsertInventarioCarro = "INSERT INTO inventario_consolidado (ubicacion_id, presentacion_id, cantidad) VALUES (?, ?, ?) " +
                                               "ON CONFLICT (ubicacion_id, presentacion_id) DO UPDATE SET cantidad = inventario_consolidado.cantidad + ?";
                jdbcTemplate.update(upsertInventarioCarro, vehiculoId, item.getPresentacionId(), item.getCantidad(), item.getCantidad());
            }

            // Actualizar estado
            jdbcTemplate.update("UPDATE viajes SET estado = 'CARGADO' WHERE id = ?", viajeId);
            jdbcTemplate.update("UPDATE vehiculos SET estado = 'EN_RUTA' WHERE id = ?", vehiculoId);

            return ResponseEntity.ok(Map.of("mensaje", "Carga confirmada y transferida al vehículo exitosamente"));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error procesando la carga: " + e.getMessage()));
        }
    }
    
    @GetMapping("/inventario-almacen")
    public ResponseEntity<?> obtenerInventarioAlmacen() {
        try {
            String sql = "SELECT ic.presentacion_id, p.nombre AS producto, pre.nombre AS presentacion, ic.cantidad, pre.precio_venta " +
                         "FROM inventario_consolidado ic " +
                         "JOIN presentaciones pre ON ic.presentacion_id = pre.id " +
                         "JOIN productos p ON pre.producto_id = p.id " +
                         "WHERE ic.ubicacion_id = 1 AND ic.cantidad > 0";
            return ResponseEntity.ok(jdbcTemplate.queryForList(sql));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    // --- NUEVO: OBTENER INVENTARIO ACTUAL DE UN VEHÍCULO ESPECÍFICO ---
    @GetMapping("/vehiculos/{idVehiculo}/inventario")
    public ResponseEntity<?> obtenerInventarioVehiculo(@PathVariable Long idVehiculo) {
        try {
            String sql = "SELECT ic.presentacion_id, p.nombre AS producto_nombre, pre.nombre AS presentacion_nombre, ic.cantidad, pre.precio_venta " +
                         "FROM inventario_consolidado ic " +
                         "JOIN presentaciones pre ON ic.presentacion_id = pre.id " +
                         "JOIN productos p ON pre.producto_id = p.id " +
                         "WHERE ic.ubicacion_id = ? AND ic.cantidad > 0";
            return ResponseEntity.ok(jdbcTemplate.queryForList(sql, idVehiculo));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(List.of());
        }
    }
}