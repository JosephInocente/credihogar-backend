package com.abarrotes.sistema.vehiculo.infrastructure.adapter;

import com.abarrotes.sistema.vehiculo.domain.model.EstadoVehiculo;
import com.abarrotes.sistema.vehiculo.domain.model.Vehiculo;
import com.abarrotes.sistema.vehiculo.domain.port.VehiculoRepositoryPort;
import com.abarrotes.sistema.vehiculo.infrastructure.entity.VehiculoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class VehiculoJpaAdapter implements VehiculoRepositoryPort {

    private final SpringDataVehiculoRepository repository;

    @Override
    public Vehiculo guardar(Vehiculo vehiculo) {
        VehiculoEntity entity = mapearAEntity(vehiculo);
        VehiculoEntity guardado = repository.save(entity);
        return mapearADominio(guardado);
    }

    @Override
    public Optional<Vehiculo> buscarPorId(Long id) {
        return repository.findById(id).map(this::mapearADominio);
    }

    @Override
    public Optional<Vehiculo> buscarPorPlaca(String placa) {
        return repository.findByPlaca(placa).map(this::mapearADominio);
    }

    @Override
    public boolean existePorPlaca(String placa) {
        return repository.existsByPlaca(placa);
    }

    @Override
    public List<Vehiculo> listarTodos() {
        return repository.findAll().stream()
                .map(this::mapearADominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Vehiculo> listarDisponibles() {
        return repository.findByEstado(EstadoVehiculo.DISPONIBLE).stream()
                .map(this::mapearADominio)
                .collect(Collectors.toList());
    }

    // --- Métodos Privados de Mapeo ---

    private VehiculoEntity mapearAEntity(Vehiculo vehiculo) {
        return VehiculoEntity.builder()
                .id(vehiculo.getId())
                .placa(vehiculo.getPlaca())
                .marca(vehiculo.getMarca())
                .modelo(vehiculo.getModelo())
                .capacidad(vehiculo.getCapacidad())
                .estado(vehiculo.getEstado())
                .observaciones(vehiculo.getObservaciones())
                .build();
    }

    private Vehiculo mapearADominio(VehiculoEntity entity) {
        return Vehiculo.builder()
                .id(entity.getId())
                .placa(entity.getPlaca())
                .marca(entity.getMarca())
                .modelo(entity.getModelo())
                .capacidad(entity.getCapacidad())
                .estado(entity.getEstado())
                .observaciones(entity.getObservaciones())
                .build();
    }
}