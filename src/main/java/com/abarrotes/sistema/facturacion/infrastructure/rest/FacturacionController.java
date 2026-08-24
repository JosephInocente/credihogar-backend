package com.abarrotes.sistema.facturacion.infrastructure.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturacion")
@RequiredArgsConstructor
public class FacturacionController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/ventas")
    public ResponseEntity<?> listarVentasParaFacturacion() {
        try {
            // CORRECCIÓN APLICADA: Ahora lee 'c.razon_social' de tu tabla clientes
            String sql = "SELECT v.id, v.viaje_id, v.fecha_hora AS fecha, v.total, " +
                         "c.razon_social AS cliente_nombre, " + // <-- AQUÍ ESTÁ EL CAMBIO
                         "c.numero_documento AS cliente_documento, " + 
                         "COALESCE(c.tipo_documento, v.tipo_documento_cliente, 'DNI') AS tipo_documento_cliente, " +
                         "v.estado_facturacion, v.comprobante_numero " +
                         "FROM ventas v " +
                         "LEFT JOIN clientes c ON v.cliente_id = c.id " +
                         "ORDER BY v.fecha_hora DESC";
                         
            List<Map<String, Object>> ventas = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(ventas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/emitir/{ventaId}")
    public ResponseEntity<?> emitirComprobante(@PathVariable Long ventaId, @RequestBody Map<String, String> request) {
        try {
            String tipo = request.get("tipo"); // 'BOLETA' o 'FACTURA'
            
            // --- ADAPTADOR DE FACTURACIÓN (SIMULACIÓN DE SUNAT / NUBEFACT) ---
            String prefijo = "FACTURA".equals(tipo) ? "F001-" : "B001-";
            String correlativo = String.format("%06d", (int)(Math.random() * 100000));
            String numeroComprobante = prefijo + correlativo;

            // Actualizamos la base de datos con la respuesta "exitosa" de la SUNAT
            String sql = "UPDATE ventas SET estado_facturacion = 'EMITIDO', comprobante_numero = ? WHERE id = ?";
            jdbcTemplate.update(sql, numeroComprobante, ventaId);

            return ResponseEntity.ok(Map.of(
                "mensaje", "Emitido correctamente",
                "comprobante", numeroComprobante
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}