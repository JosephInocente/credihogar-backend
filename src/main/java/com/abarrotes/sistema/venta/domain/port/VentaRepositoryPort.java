package com.abarrotes.sistema.venta.domain.port;

import com.abarrotes.sistema.venta.domain.model.Venta;
import java.util.List;
import java.util.Optional;

public interface VentaRepositoryPort {
    Venta guardar(Venta venta);
    Optional<Venta> buscarPorId(Long id);
    List<Venta> listarPorViaje(Long viajeId);
}