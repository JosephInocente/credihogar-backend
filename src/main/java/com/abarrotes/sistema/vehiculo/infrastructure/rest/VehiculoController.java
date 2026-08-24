package com.abarrotes.sistema.vehiculo.infrastructure.rest;

import com.abarrotes.sistema.vehiculo.application.port.GestionarVehiculoUseCase;
import com.abarrotes.sistema.vehiculo.domain.model.Vehiculo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final GestionarVehiculoUseCase gestionarVehiculoUseCase;

    @PostMapping
    public ResponseEntity<?> registrarVehiculo(@RequestBody CrearVehiculoRequest request) {
        try {
            // 1. Mapear del DTO (Web) al Modelo de Dominio (Puro)
            Vehiculo nuevoVehiculo = Vehiculo.builder()
                    .placa(request.getPlaca())
                    .marca(request.getMarca())
                    .modelo(request.getModelo())
                    .capacidad(request.getCapacidad())
                    .observaciones(request.getObservaciones())
                    .build();

            // 2. Enviar el dominio al Caso de Uso
            Vehiculo vehiculoRegistrado = gestionarVehiculoUseCase.registrarVehiculo(nuevoVehiculo);

            // 3. Retornar éxito
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Vehículo registrado exitosamente con ID: " + vehiculoRegistrado.getId());

        } catch (IllegalArgumentException e) {
            // Si la placa ya existe, retornamos un 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Vehiculo>> listarVehiculos() {
        return ResponseEntity.ok(gestionarVehiculoUseCase.listarVehiculos());
    }
    
    @GetMapping("/disponibles")
    public ResponseEntity<List<Vehiculo>> listarVehiculosDisponibles() {
        return ResponseEntity.ok(gestionarVehiculoUseCase.listarVehiculosDisponibles());
    }
}