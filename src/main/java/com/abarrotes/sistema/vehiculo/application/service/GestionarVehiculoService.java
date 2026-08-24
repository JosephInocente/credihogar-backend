package com.abarrotes.sistema.vehiculo.application.service;

import com.abarrotes.sistema.vehiculo.application.port.GestionarVehiculoUseCase;
import com.abarrotes.sistema.vehiculo.domain.model.EstadoVehiculo;
import com.abarrotes.sistema.vehiculo.domain.model.Vehiculo;
import com.abarrotes.sistema.vehiculo.domain.port.VehiculoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GestionarVehiculoService implements GestionarVehiculoUseCase {

    private final VehiculoRepositoryPort vehiculoRepository;

    @Override
    public Vehiculo registrarVehiculo(Vehiculo vehiculo) {
        
        // 1. Regla de negocio: La placa debe ser única (HU-09)
        if (vehiculoRepository.existePorPlaca(vehiculo.getPlaca())) {
            throw new IllegalArgumentException("Ya existe un vehículo registrado con la placa: " + vehiculo.getPlaca());
        }

        // 2. Estado inicial por defecto: Todo carro nuevo está DISPONIBLE
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);

        // 3. Guardar en la base de datos a través del puerto
        return vehiculoRepository.guardar(vehiculo);
    }

    @Override
    public List<Vehiculo> listarVehiculos() {
        return vehiculoRepository.listarTodos();
    }

    @Override
    public List<Vehiculo> listarVehiculosDisponibles() {
        return vehiculoRepository.listarDisponibles();
    }
}
