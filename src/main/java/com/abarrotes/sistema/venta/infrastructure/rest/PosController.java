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
@RequestMapping("/api/pos")
@RequiredArgsConstructor
public class PosController {

    private final JdbcTemplate jdbcTemplate;

    @Data
    public static class ProductoPosDTO {
        private Long id; 
        private String nombre;
        private String sku;
        private Double precio;
        private Integer stock;
    }

    @Data
    public static class VentaDetalleRequestDTO {
        private Long inventarioId;
        private Integer cantidad;
        private Double precioUnitario;
        private Double subtotal; 
    }

    @Data
    public static class VentaRequestDTO {
        private String clienteDocumento;
        private String clienteNombre;
        private Double totalVenta;
        private Long usuarioId; // <-- NUEVO: Para saber quién vende
        private List<VentaDetalleRequestDTO> detalles;
    }

    // --- NUEVO: Recibe el ID del usuario y su ROL para saber qué inventario mostrar ---
    @GetMapping("/productos")
    public ResponseEntity<?> obtenerProductosParaVenta(
            @RequestParam(required = false) Long usuarioId, 
            @RequestParam(required = false) String rol) {
        
        Long ubicacionId = 1L; // Almacén Principal por defecto (Para el Gerente)
        
        // Si es Gestor, buscamos en qué camión está subido actualmente
        if ("GESTOR".equals(rol) && usuarioId != null) {
            try {
                String sqlViaje = "SELECT vehiculo_id FROM viajes WHERE gestor_id = ? AND estado IN ('CARGADO', 'EN_RUTA') LIMIT 1";
                ubicacionId = jdbcTemplate.queryForObject(sqlViaje, Long.class, usuarioId);
            } catch (EmptyResultDataAccessException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "No tienes ningún viaje activo o camión asignado en este momento."));
            }
        }

        String sql = "SELECT " +
                     "i.id, " +
                     "p.nombre || ' - ' || pre.nombre AS nombre, " +
                     "p.sku, " +
                     "i.cantidad AS stock, " +
                     "pre.precio_venta AS precio " + 
                     "FROM inventario_consolidado i " +
                     "JOIN presentaciones pre ON i.presentacion_id = pre.id " +
                     "JOIN productos p ON pre.producto_id = p.id " +
                     "WHERE i.ubicacion_id = ? AND i.cantidad > 0";

        List<ProductoPosDTO> lista = jdbcTemplate.query(sql, (rs, rowNum) -> {
            ProductoPosDTO dto = new ProductoPosDTO();
            dto.setId(rs.getLong("id")); 
            dto.setNombre(rs.getString("nombre"));
            dto.setSku(rs.getString("sku"));
            dto.setPrecio(rs.getDouble("precio"));
            dto.setStock(rs.getInt("stock"));
            return dto;
        }, ubicacionId);

        return ResponseEntity.ok(lista);
    }

    @PostMapping("/procesar")
    @Transactional 
    public ResponseEntity<?> procesarVenta(@RequestBody VentaRequestDTO request) {
        try {
            Long clienteId = 1L; 
            
            if (request.getClienteDocumento() != null && !request.getClienteDocumento().isBlank()) {
                try {
                    String sqlBusquedaCliente = "SELECT id FROM clientes WHERE numero_documento = ?";
                    clienteId = jdbcTemplate.queryForObject(sqlBusquedaCliente, Long.class, request.getClienteDocumento());
                } catch (EmptyResultDataAccessException e) {
                    String tipoDoc = request.getClienteDocumento().length() == 11 ? "RUC" : "DNI";
                    String nombreReal = request.getClienteNombre() != null && !request.getClienteNombre().isBlank() ? request.getClienteNombre() : "CLIENTE " + request.getClienteDocumento();
                    String sqlInsertCliente = "INSERT INTO clientes (tipo_documento, numero_documento, razon_social, direccion) VALUES (?, ?, ?, ?) RETURNING id";
                    clienteId = jdbcTemplate.queryForObject(sqlInsertCliente, Long.class, tipoDoc, request.getClienteDocumento(), nombreReal, "No especificada");
                }
            }

            // NUEVO: Identificamos quién vende y si está en un viaje
            Long trabajadorId = request.getUsuarioId() != null ? request.getUsuarioId() : 2L; 
            Long viajeId = null;
            String estado = "EMITIDA";

            try {
                // Si es un Gestor en ruta, capturamos su viaje actual para ligar la venta a la liquidación
                String sqlViaje = "SELECT id FROM viajes WHERE gestor_id = ? AND estado IN ('CARGADO', 'EN_RUTA') LIMIT 1";
                viajeId = jdbcTemplate.queryForObject(sqlViaje, Long.class, trabajadorId);
            } catch (Exception e) {
                // Si falla, es porque es el Gerente vendiendo en el Almacén Principal. No pasa nada.
            }
            
            Long ventaId;
            if (viajeId != null) {
                String insertVentaSql = "INSERT INTO ventas (cliente_id, estado, fecha_hora, total, trabajador_id, viaje_id) VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?, ?) RETURNING id";
                ventaId = jdbcTemplate.queryForObject(insertVentaSql, Long.class, clienteId, estado, request.getTotalVenta(), trabajadorId, viajeId);
            } else {
                String insertVentaSql = "INSERT INTO ventas (cliente_id, estado, fecha_hora, total, trabajador_id) VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?) RETURNING id";
                ventaId = jdbcTemplate.queryForObject(insertVentaSql, Long.class, clienteId, estado, request.getTotalVenta(), trabajadorId);
            }

            for (VentaDetalleRequestDTO detalle : request.getDetalles()) {
                String sqlGetPresentacion = "SELECT presentacion_id FROM inventario_consolidado WHERE id = ?";
                Long presentacionId = jdbcTemplate.queryForObject(sqlGetPresentacion, Long.class, detalle.getInventarioId());

                String insertDetalleSql = "INSERT INTO venta_detalles (cantidad, precio_unitario, presentacion_id, venta_id) VALUES (?, ?, ?, ?)";
                jdbcTemplate.update(insertDetalleSql, detalle.getCantidad(), detalle.getPrecioUnitario(), presentacionId, ventaId);

                // IMPORTANTE: Al usar el 'inventarioId', se descuenta EXACTAMENTE del camión correcto o del almacén principal
                String updateStockSql = "UPDATE inventario_consolidado SET cantidad = cantidad - ? WHERE id = ?";
                jdbcTemplate.update(updateStockSql, detalle.getCantidad(), detalle.getInventarioId());
            }

            return ResponseEntity.ok().body(Map.of("mensaje", "Venta procesada exitosamente", "ventaId", ventaId, "clienteId", clienteId));

        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al procesar la venta: " + e.getMessage()));
        }
    }
}