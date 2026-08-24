package com.abarrotes.sistema.viaje.infrastructure.rest;

import com.abarrotes.sistema.viaje.application.port.GestionarViajeUseCase;
import com.abarrotes.sistema.viaje.domain.model.DetalleViaje;
import com.abarrotes.sistema.viaje.domain.model.Viaje;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/viajes")
@RequiredArgsConstructor
public class ViajeController {

    private final GestionarViajeUseCase gestionarViajeUseCase;

    @PostMapping
    public ResponseEntity<?> crearViaje(@RequestBody CrearViajeRequest request) {
        try {
            Viaje nuevoViaje = Viaje.builder()
                    .fecha(request.getFecha())
                    .destino(request.getDestino())
                    .almacenOrigenId(request.getAlmacenOrigenId())
                    .vehiculoId(request.getVehiculoId())
                    .trabajadorId(request.getTrabajadorId())
                    .build();

            Viaje viajeCreado = gestionarViajeUseCase.crearViaje(nuevoViaje);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Viaje creado exitosamente en estado BORRADOR con ID: " + viajeCreado.getId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    // NUEVO: Endpoint para agregar productos al viaje (Guía de Carga)
    @PostMapping("/{id}/carga")
    public ResponseEntity<?> agregarCarga(@PathVariable("id") Long viajeId, @RequestBody AgregarCargaRequest request) {
        try {
            // Convertimos los DTOs al modelo de Dominio
            List<DetalleViaje> detalles = request.getDetalles().stream()
                    .map(dto -> DetalleViaje.builder()
                            .presentacionId(dto.getPresentacionId())
                            .cantidad(dto.getCantidad())
                            .build())
                    .collect(Collectors.toList());

            gestionarViajeUseCase.agregarCarga(viajeId, detalles);
            
            return ResponseEntity.ok("Carga agregada exitosamente al viaje ID: " + viajeId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    // NUEVO: Endpoint para confirmar la carga y descontar inventario
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmarCarga(@PathVariable("id") Long viajeId) {
        try {
            // Extraemos el nombre del gerente logueado para la auditoría de inventario
            String usuarioLogueado = SecurityContextHolder.getContext().getAuthentication().getName();

            // Ejecutamos el caso de uso transaccional
            gestionarViajeUseCase.confirmarCargaYDescontarStock(viajeId, usuarioLogueado);

            return ResponseEntity.ok("Carga confirmada. El estado del viaje es CARGADO y el stock fue descontado.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }
    
 // NUEVO: Endpoint para Liquidar el viaje y retornar stock
    @PostMapping("/{id}/liquidar")
    public ResponseEntity<?> liquidarViaje(@PathVariable("id") Long viajeId) {
        try {
            // Quien liquida suele ser el GERENTE o el administrador de almacén
            String usuarioLogueado = SecurityContextHolder.getContext().getAuthentication().getName();

            gestionarViajeUseCase.liquidarViaje(viajeId, usuarioLogueado);

            return ResponseEntity.ok("Viaje liquidado con éxito. Vehículo liberado y stock sobrante retornado al almacén.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }
}