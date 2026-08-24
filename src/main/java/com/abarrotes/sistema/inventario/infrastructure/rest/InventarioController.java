package com.abarrotes.sistema.inventario.infrastructure.rest;

import com.abarrotes.sistema.inventario.application.port.GestionarInventarioUseCase;
import com.abarrotes.sistema.inventario.domain.model.Ubicacion;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final GestionarInventarioUseCase gestionarInventarioUseCase;

    @PostMapping("/ubicaciones")
    public ResponseEntity<?> registrarUbicacion(@RequestBody CrearUbicacionRequest request) {
        Ubicacion ubicacion = gestionarInventarioUseCase.registrarUbicacion(
                request.getNombre(), request.getTipo()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Ubicación creada con ID: " + ubicacion.getId()));
    }

    @PostMapping("/entradas")
    public ResponseEntity<?> registrarEntrada(@RequestBody RegistrarEntradaRequest request) {
        try {
            // Extraer el nombre del usuario autenticado directamente desde el token JWT
            String usuarioLogueado = SecurityContextHolder.getContext().getAuthentication().getName();

            // Anexar el documento de referencia al motivo si existe (RF26)
            String motivoFinal = request.getMotivo();
            if (request.getDocumentoReferencia() != null && !request.getDocumentoReferencia().isBlank()) {
                motivoFinal += " - Doc: " + request.getDocumentoReferencia();
            }

            // Si el frontend no envía la ubicación, forzamos el Almacén Principal (ID 1)
            Long ubicacionId = request.getUbicacionDestinoId() != null ? request.getUbicacionDestinoId() : 1L;

            // Delegar al caso de uso intacto
            gestionarInventarioUseCase.registrarEntrada(
                    ubicacionId,
                    request.getPresentacionId(),
                    request.getCantidad(),
                    usuarioLogueado,
                    motivoFinal
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensaje", "Entrada de inventario registrada con éxito"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}