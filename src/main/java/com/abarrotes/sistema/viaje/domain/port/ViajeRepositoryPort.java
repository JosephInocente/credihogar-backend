package com.abarrotes.sistema.viaje.domain.port;

import com.abarrotes.sistema.viaje.domain.model.Viaje;
import java.util.Optional;

public interface ViajeRepositoryPort {
    Viaje guardar(Viaje viaje);
    Optional<Viaje> buscarPorId(Long id);
    
    // Validaciones cruciales para RF40
    boolean existeViajeActivoParaVehiculo(Long vehiculoId);
    boolean existeViajeActivoParaTrabajador(Long trabajadorId);
}
