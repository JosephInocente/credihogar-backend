package com.abarrotes.sistema.producto.infrastructure.rest;

import com.abarrotes.sistema.producto.application.port.GestionarProductoUseCase;
import com.abarrotes.sistema.producto.domain.model.Presentacion;
import com.abarrotes.sistema.producto.domain.model.Producto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final GestionarProductoUseCase gestionarProductoUseCase;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping
    public ResponseEntity<?> crearProducto(@RequestBody CrearProductoRequest request) {
        try {
            Producto nuevoProducto = Producto.builder()
                    .sku(request.getSku())
                    .nombre(request.getNombre())
                    .categoria(request.getCategoria())
                    .marca(request.getMarca())
                    .build();

            if (request.getPresentaciones() != null) {
                for (PresentacionDTO pDto : request.getPresentaciones()) {
                    Presentacion presentacion = Presentacion.builder()
                            .nombre(pDto.getNombre())
                            .unidadBase(pDto.getUnidadBase())
                            .factorConversion(pDto.getFactorConversion())
                            .codigoBarras(pDto.getCodigoBarras())
                            .precioCompraReferencial(pDto.getPrecioCompraReferencial())
                            .precioVenta(pDto.getPrecioVenta())
                            .build();
                    nuevoProducto.agregarPresentacion(presentacion);
                }
            }

            Producto productoCreado = gestionarProductoUseCase.crearProducto(nuevoProducto);

            if (request.getImagenUrl() != null && !request.getImagenUrl().isBlank()) {
                jdbcTemplate.update("UPDATE productos SET imagen_url = ? WHERE id = ?", 
                        request.getImagenUrl(), productoCreado.getId());
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensaje", "Producto creado exitosamente"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listarProductosVisual() {
        try {
            String sqlProductos = "SELECT id, sku, nombre, marca, categoria, estado, imagen_url FROM productos WHERE estado = 'ACTIVO' ORDER BY nombre";
            List<Map<String, Object>> productos = jdbcTemplate.queryForList(sqlProductos);

            for (Map<String, Object> prod : productos) {
                String sqlPresentaciones = "SELECT * FROM presentaciones WHERE producto_id = ?";
                List<Map<String, Object>> presentaciones = jdbcTemplate.queryForList(sqlPresentaciones, prod.get("id"));
                prod.put("presentaciones", presentaciones);
            }
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{productoId}/presentaciones")
    public ResponseEntity<?> crearPresentacionRapida(@PathVariable Long productoId, @RequestBody Map<String, Object> request) {
        try {
            String sql = "INSERT INTO presentaciones (producto_id, nombre, factor_conversion, precio_venta, precio_compra_referencial, codigo_barras, unidad_base) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                    productoId,
                    request.get("nombre"),
                    request.get("factor_conversion"),
                    request.get("precio_venta"),
                    request.get("precio_compra_referencial"),
                    request.get("codigo_barras"),
                    request.get("unidad_base")
            );
            return ResponseEntity.ok(Map.of("mensaje", "Presentación y precio agregados exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ================= NUEVOS ENDPOINTS (ACTUALIZAR Y ELIMINAR) ================= //

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Long id, @RequestBody CrearProductoRequest request) {
        try {
            String sql = "UPDATE productos SET sku = ?, nombre = ?, marca = ?, categoria = ?, imagen_url = ? WHERE id = ?";
            jdbcTemplate.update(sql, request.getSku(), request.getNombre(), request.getMarca(), request.getCategoria(), request.getImagenUrl(), id);
            return ResponseEntity.ok(Map.of("mensaje", "Producto actualizado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivarProducto(@PathVariable Long id) {
        try {
            // Borrado lógico para cumplir RF11 y no dañar el historial de facturación
            jdbcTemplate.update("UPDATE productos SET estado = 'INACTIVO' WHERE id = ?", id);
            return ResponseEntity.ok(Map.of("mensaje", "Producto eliminado del catálogo"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/presentaciones/{id}")
    public ResponseEntity<?> eliminarPresentacion(@PathVariable Long id) {
        try {
            jdbcTemplate.update("DELETE FROM presentaciones WHERE id = ?", id);
            return ResponseEntity.ok(Map.of("mensaje", "Presentación eliminada"));
        } catch (Exception e) {
            // Si salta error de Integridad (FK), significa que esta presentación ya tiene stock o ventas registradas
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "No se puede eliminar porque ya tiene inventario o ventas asociadas."));
        }
    }
}