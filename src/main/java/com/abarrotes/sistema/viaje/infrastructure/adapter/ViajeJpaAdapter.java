package com.abarrotes.sistema.viaje.infrastructure.adapter;

import com.abarrotes.sistema.viaje.domain.model.EstadoViaje;
import com.abarrotes.sistema.viaje.domain.model.Viaje;
import com.abarrotes.sistema.viaje.domain.port.ViajeRepositoryPort;
import com.abarrotes.sistema.viaje.infrastructure.entity.DetalleViajeEntity;
import com.abarrotes.sistema.viaje.infrastructure.entity.ViajeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ViajeJpaAdapter implements ViajeRepositoryPort {

    private final SpringDataViajeRepository repository;

    @Override
    public Viaje guardar(Viaje viaje) {
        ViajeEntity entity = mapearAEntity(viaje);
        ViajeEntity guardado = repository.save(entity);
        return mapearADominio(guardado);
    }

    @Override
    public Optional<Viaje> buscarPorId(Long id) {
        return repository.findById(id).map(this::mapearADominio);
    }

    @Override
    public boolean existeViajeActivoParaVehiculo(Long vehiculoId) {
        // Un viaje está activo si su estado NO es CERRADO
        return repository.existsByVehiculoIdAndEstadoNotIn(vehiculoId, List.of(EstadoViaje.CERRADO));
    }

    @Override
    public boolean existeViajeActivoParaTrabajador(Long trabajadorId) {
        // Un viaje está activo si su estado NO es CERRADO
        return repository.existsByTrabajadorIdAndEstadoNotIn(trabajadorId, List.of(EstadoViaje.CERRADO));
    }

 // --- Métodos de Mapeo ---

    private ViajeEntity mapearAEntity(Viaje viaje) {
        ViajeEntity entity = ViajeEntity.builder()
                .id(viaje.getId())
                .fecha(viaje.getFecha())
                .destino(viaje.getDestino())
                .almacenOrigenId(viaje.getAlmacenOrigenId())
                .vehiculoId(viaje.getVehiculoId())
                .trabajadorId(viaje.getTrabajadorId())
                .estado(viaje.getEstado())
                .build();

        // Mapear los detalles si existen
        if (viaje.getDetalles() != null) {
            for (com.abarrotes.sistema.viaje.domain.model.DetalleViaje detDominio : viaje.getDetalles()) {
                DetalleViajeEntity detEntity = DetalleViajeEntity.builder()
                        .id(detDominio.getId())
                        .presentacionId(detDominio.getPresentacionId())
                        .cantidad(detDominio.getCantidad())
                        .build();
                entity.addDetalle(detEntity);
            }
        }
        return entity;
    }

    private Viaje mapearADominio(ViajeEntity entity) {
        Viaje viaje = Viaje.builder()
                .id(entity.getId())
                .fecha(entity.getFecha())
                .destino(entity.getDestino())
                .almacenOrigenId(entity.getAlmacenOrigenId())
                .vehiculoId(entity.getVehiculoId())
                .trabajadorId(entity.getTrabajadorId())
                .estado(entity.getEstado())
                .build();

        if (entity.getDetalles() != null) {
            entity.getDetalles().forEach(detEntity -> 
                viaje.agregarDetalle(com.abarrotes.sistema.viaje.domain.model.DetalleViaje.builder()
                        .id(detEntity.getId())
                        .presentacionId(detEntity.getPresentacionId())
                        .cantidad(detEntity.getCantidad())
                        .build())
            );
        }
        return viaje;
    }
}