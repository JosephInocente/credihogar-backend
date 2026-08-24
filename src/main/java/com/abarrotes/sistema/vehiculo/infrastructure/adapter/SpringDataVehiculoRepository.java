package com.abarrotes.sistema.vehiculo.infrastructure.adapter;

import com.abarrotes.sistema.vehiculo.domain.model.EstadoVehiculo;
import com.abarrotes.sistema.vehiculo.infrastructure.entity.VehiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataVehiculoRepository extends JpaRepository<VehiculoEntity, Long> {
    
    Optional<VehiculoEntity> findByPlaca(String placa);
    
    boolean existsByPlaca(String placa);
    
    List<VehiculoEntity> findByEstado(EstadoVehiculo estado);
}
