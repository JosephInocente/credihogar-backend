package com.abarrotes.sistema.venta.infrastructure.rest;

import com.abarrotes.sistema.venta.application.port.GestionarVentaUseCase;
import com.abarrotes.sistema.venta.domain.model.DetalleVenta;
import com.abarrotes.sistema.venta.domain.model.Venta;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final GestionarVentaUseCase gestionarVentaUseCase;

    @PostMapping
    public ResponseEntity<?> registrarVenta(@RequestBody CrearVentaRequest request) {
        try {
            // 1. Mapear los detalles del DTO al Dominio
            List<DetalleVenta> detallesDominio = request.getDetalles().stream()
                    .map(dto -> DetalleVenta.builder()
                            .presentacionId(dto.getPresentacionId())
                            .cantidad(dto.getCantidad())
                            .precioUnitario(dto.getPrecioUnitario())
                            .build())
                    .collect(Collectors.toList());

            // 2. Construir la Venta de Dominio
            Venta nuevaVenta = Venta.builder()
                    .clienteId(request.getClienteId())
                    .trabajadorId(request.getTrabajadorId())
                    .viajeId(request.getViajeId())
                    .detalles(detallesDominio)
                    .build();

            // 3. Ejecutar el caso de uso
            Venta ventaRegistrada = gestionarVentaUseCase.registrarVenta(nuevaVenta);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Venta registrada exitosamente con ID: " + ventaRegistrada.getId() + 
                          " | Total calculado: S/ " + ventaRegistrada.getTotal());

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/viaje/{viajeId}")
    public ResponseEntity<List<Venta>> listarPorViaje(@PathVariable("viajeId") Long viajeId) {
        return ResponseEntity.ok(gestionarVentaUseCase.listarVentasPorViaje(viajeId));
    }
}