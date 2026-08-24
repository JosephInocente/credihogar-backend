package com.abarrotes.sistema.inventario.infrastructure.rest;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario/vista")
@RequiredArgsConstructor
public class InventarioConsultaController {

    private final JdbcTemplate jdbcTemplate;

    @Data
    public static class InventarioRowDTO {
        private Long id;
        private String sku;
        private String producto;
        private String presentacion;
        private String ubicacion;
        private Integer cantidad;
        private String estado;
    }

    @GetMapping
    public ResponseEntity<List<InventarioRowDTO>> obtenerVistaInventario() {
        String sql = "SELECT " +
                     "i.id, " +
                     "p.sku, " +
                     "p.nombre AS producto, " +
                     "pre.nombre AS presentacion, " +
                     "CASE " +
                     "  WHEN i.ubicacion_id = 1 THEN 'Almacén Principal' " +
                     "  ELSE 'Carro ' || v.placa " +
                     "END AS ubicacion, " +
                     "i.cantidad, " +
                     "CASE " +
                     "  WHEN i.cantidad > 20 THEN 'ÓPTIMO' " +
                     "  WHEN i.cantidad >= 10 THEN 'BAJO STOCK' " +
                     "  ELSE 'CRÍTICO' " +
                     "END AS estado " +
                     "FROM inventario_consolidado i " +
                     "JOIN presentaciones pre ON i.presentacion_id = pre.id " +
                     "JOIN productos p ON pre.producto_id = p.id " +
                     "LEFT JOIN vehiculos v ON i.ubicacion_id = v.id";

        List<InventarioRowDTO> lista = jdbcTemplate.query(sql, (rs, rowNum) -> {
            InventarioRowDTO dto = new InventarioRowDTO();
            dto.setId(rs.getLong("id"));
            dto.setSku(rs.getString("sku"));
            dto.setProducto(rs.getString("producto"));
            dto.setPresentacion(rs.getString("presentacion"));
            dto.setUbicacion(rs.getString("ubicacion"));
            dto.setCantidad(rs.getInt("cantidad"));
            dto.setEstado(rs.getString("estado"));
            return dto;
        });

        return ResponseEntity.ok(lista);
    }

    // NUEVO: Endpoint para llenar el selector del frontend al registrar entrada
    @GetMapping("/presentaciones")
    public ResponseEntity<?> obtenerPresentaciones() {
        String sql = "SELECT pre.id, p.nombre AS producto, pre.nombre AS presentacion, p.sku " +
                     "FROM presentaciones pre " +
                     "JOIN productos p ON pre.producto_id = p.id " +
                     "WHERE p.estado = 'ACTIVO'";
        try {
            return ResponseEntity.ok(jdbcTemplate.queryForList(sql));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}