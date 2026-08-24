package com.abarrotes.sistema.vehiculo.domain.port;

import com.abarrotes.sistema.vehiculo.domain.model.Vehiculo;
import java.util.List;
import java.util.Optional;

public interface VehiculoRepositoryPort {
    Vehiculo guardar(Vehiculo vehiculo);
    Optional<Vehiculo> buscarPorId(Long id);
    Optional<Vehiculo> buscarPorPlaca(String placa);
    boolean existePorPlaca(String placa);
    List<Vehiculo> listarTodos();
    List<Vehiculo> listarDisponibles();
}
