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
        private Long id; // ID de inventario_consolidado
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
        private Double subtotal; // Lo recibimos de React pero no lo guardamos en BD según tu esquema
    }

    @Data
    public static class VentaRequestDTO {
        private String clienteDocumento;
        private String clienteNombre;
        private Double totalVenta;
        private List<VentaDetalleRequestDTO> detalles;
    }

    @GetMapping("/productos")
    public ResponseEntity<List<ProductoPosDTO>> obtenerProductosParaVenta() {
        // CORRECCIÓN: Se agregó "i.ubicacion_id = 1" para filtrar solo el Almacén Principal
        String sql = "SELECT " +
                     "i.id, " +
                     "p.nombre || ' - ' || pre.nombre AS nombre, " +
                     "p.sku, " +
                     "i.cantidad AS stock, " +
                     "pre.precio_venta AS precio " + 
                     "FROM inventario_consolidado i " +
                     "JOIN presentaciones pre ON i.presentacion_id = pre.id " +
                     "JOIN productos p ON pre.producto_id = p.id " +
                     "WHERE i.ubicacion_id = 1 AND i.cantidad > 0";

        List<ProductoPosDTO> lista = jdbcTemplate.query(sql, (rs, rowNum) -> {
            ProductoPosDTO dto = new ProductoPosDTO();
            dto.setId(rs.getLong("id")); 
            dto.setNombre(rs.getString("nombre"));
            dto.setSku(rs.getString("sku"));
            dto.setPrecio(rs.getDouble("precio"));
            dto.setStock(rs.getInt("stock"));
            return dto;
        });

        return ResponseEntity.ok(lista);
    }

    @PostMapping("/procesar")
    @Transactional // Si algo falla (ej. error de stock), revierte todo automáticamente
    public ResponseEntity<?> procesarVenta(@RequestBody VentaRequestDTO request) {
        try {
            // 1. GESTIÓN DEL CLIENTE (Búsqueda o Inserción Automática)
            Long clienteId = 1L; // ID por defecto en caso de "Cliente Genérico" / Público en General
            
            if (request.getClienteDocumento() != null && !request.getClienteDocumento().isBlank()) {
                try {
                    // Verificamos si el cliente ya existe en nuestra base de datos
                    String sqlBusquedaCliente = "SELECT id FROM clientes WHERE numero_documento = ?";
                    clienteId = jdbcTemplate.queryForObject(sqlBusquedaCliente, Long.class, request.getClienteDocumento());
                } catch (EmptyResultDataAccessException e) {
                    // Si no existe, lo insertamos silenciosamente para no interrumpir la venta
                    String tipoDoc = request.getClienteDocumento().length() == 11 ? "RUC" : "DNI";
                    String nombreReal = request.getClienteNombre() != null && !request.getClienteNombre().isBlank() ? request.getClienteNombre() : "CLIENTE " + request.getClienteDocumento();
                    
                    String sqlInsertCliente = "INSERT INTO clientes (tipo_documento, numero_documento, razon_social, direccion) VALUES (?, ?, ?, ?) RETURNING id";
                    clienteId = jdbcTemplate.queryForObject(sqlInsertCliente, Long.class, tipoDoc, request.getClienteDocumento(), nombreReal, "No especificada");
                }
            }

            // 2. CREACIÓN DE LA VENTA (Tabla 'ventas')
            // NOTA: Usamos el trabajador_id = 2 temporalmente (que vi en tu base de datos). 
            // En el futuro, esto se extraerá del token JWT de Spring Security.
            Long trabajadorId = 2L; 
            String estado = "EMITIDA";
            
            String insertVentaSql = "INSERT INTO ventas (cliente_id, estado, fecha_hora, total, trabajador_id) VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?) RETURNING id";
            Long ventaId = jdbcTemplate.queryForObject(insertVentaSql, Long.class, clienteId, estado, request.getTotalVenta(), trabajadorId);

            // 3. PROCESAMIENTO DE LOS DETALLES Y DESCUENTO DE INVENTARIO
            for (VentaDetalleRequestDTO detalle : request.getDetalles()) {
                
                // Primero averiguamos a qué 'presentacion_id' pertenece este ítem del inventario
                String sqlGetPresentacion = "SELECT presentacion_id FROM inventario_consolidado WHERE id = ?";
                Long presentacionId = jdbcTemplate.queryForObject(sqlGetPresentacion, Long.class, detalle.getInventarioId());

                // Guardamos en 'venta_detalles' respetando tus columnas exactas (sin subtotal)
                String insertDetalleSql = "INSERT INTO venta_detalles (cantidad, precio_unitario, presentacion_id, venta_id) VALUES (?, ?, ?, ?)";
                jdbcTemplate.update(insertDetalleSql, detalle.getCantidad(), detalle.getPrecioUnitario(), presentacionId, ventaId);

                // Descontamos físicamente el stock de 'inventario_consolidado'
                String updateStockSql = "UPDATE inventario_consolidado SET cantidad = cantidad - ? WHERE id = ?";
                jdbcTemplate.update(updateStockSql, detalle.getCantidad(), detalle.getInventarioId());
            }

            return ResponseEntity.ok().body(Map.of(
                "mensaje", "Venta procesada exitosamente", 
                "ventaId", ventaId,
                "clienteId", clienteId
            ));

        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al procesar la venta: " + e.getMessage()));
        }
    }
}