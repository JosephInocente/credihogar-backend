package com.abarrotes.sistema.viaje.infrastructure.adapter;

import com.abarrotes.sistema.viaje.domain.model.EstadoViaje;
import com.abarrotes.sistema.viaje.infrastructure.entity.ViajeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface SpringDataViajeRepository extends JpaRepository<ViajeEntity, Long> {
    
    // Verifica si el vehículo tiene algún viaje que no esté en los estados indicados (ej. CERRADO)
    boolean existsByVehiculoIdAndEstadoNotIn(Long vehiculoId, Collection<EstadoViaje> estados);
    
    // Verifica si el trabajador tiene algún viaje que no esté cerrado
    boolean existsByTrabajadorIdAndEstadoNotIn(Long trabajadorId, Collection<EstadoViaje> estados);
}
