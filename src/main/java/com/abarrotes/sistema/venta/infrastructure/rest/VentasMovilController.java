package com.abarrotes.sistema.venta.infrastructure.rest;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas-movil")
@RequiredArgsConstructor
public class VentasMovilController {

    private final JdbcTemplate jdbcTemplate;

    @Data
    public static class ItemVentaDTO {
        private Long presentacionId;
        private Integer cantidad;
        private Double precioBase;
        private Double precioFinal;
    }

    @Data
    public static class VentaRequestDTO {
        private Long viajeId;
        private Long vehiculoId;
        private Long trabajadorId;
        private String clienteDocumento; 
        private String clienteNombre;    
        private Double total;
        private List<ItemVentaDTO> items;
    }

    @GetMapping("/mi-viaje/{username}")
    public ResponseEntity<?> obtenerMiViajeActivo(@PathVariable String username) {
        try {
            String sql = "SELECT v.id, v.vehiculo_id, v.trabajador_id, veh.placa, v.destino, v.estado " +
                         "FROM viajes v " +
                         "JOIN vehiculos veh ON v.vehiculo_id = veh.id " +
                         "JOIN usuarios u ON v.trabajador_id = u.id " +
                         "WHERE u.username = ? AND v.estado IN ('CARGADO', 'EN_RUTA') " +
                         "ORDER BY v.fecha DESC LIMIT 1";
            Map<String, Object> viaje = jdbcTemplate.queryForMap(sql, username);
            return ResponseEntity.ok(viaje);
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.ok(Map.of("mensaje", "No tienes viajes activos en ruta."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

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

    @PostMapping("/registrar")
    @Transactional
    public ResponseEntity<?> registrarVenta(@RequestBody VentaRequestDTO request) {
        try {
            // 1. ELIMINAMOS EL HARDCODEO: Validamos los datos o forzamos "Público en General"
            String docBuscado = (request.getClienteDocumento() != null && !request.getClienteDocumento().isBlank()) 
                                ? request.getClienteDocumento() 
                                : "00000000";
                                
            String nombreBuscado = (request.getClienteNombre() != null && !request.getClienteNombre().isBlank()) 
                                   ? request.getClienteNombre() 
                                   : "Público en General";

            Long clienteIdFinal;
            
            try {
                // 2. Buscamos exactamente el documento que llegó (incluyendo el 00000000)
                String sqlBusquedaCliente = "SELECT id FROM clientes WHERE numero_documento = ?";
                clienteIdFinal = jdbcTemplate.queryForObject(sqlBusquedaCliente, Long.class, docBuscado);
            } catch (EmptyResultDataAccessException e) {
                // 3. Si no existe en BD, el sistema lo crea dinámicamente y obtiene su nuevo ID
                String tipoDoc = docBuscado.length() == 11 ? "RUC" : "DNI";
                String sqlInsertCliente = "INSERT INTO clientes (tipo_documento, numero_documento, razon_social, direccion) VALUES (?, ?, ?, ?) RETURNING id";
                clienteIdFinal = jdbcTemplate.queryForObject(sqlInsertCliente, Long.class, tipoDoc, docBuscado, nombreBuscado, "No especificada");
            }

            String sqlVenta = "INSERT INTO ventas (viaje_id, cliente_id, total, fecha_hora, estado, trabajador_id) VALUES (?, ?, ?, CURRENT_TIMESTAMP, 'EMITIDA', ?) RETURNING id";
            
            Long ventaId = jdbcTemplate.queryForObject(sqlVenta, Long.class, 
                request.getViajeId(), 
                clienteIdFinal, 
                request.getTotal(), 
                request.getTrabajadorId()); 

            for (ItemVentaDTO item : request.getItems()) {
                jdbcTemplate.update("INSERT INTO venta_detalles (venta_id, presentacion_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?)",
                    ventaId, item.getPresentacionId(), item.getCantidad(), item.getPrecioFinal());

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