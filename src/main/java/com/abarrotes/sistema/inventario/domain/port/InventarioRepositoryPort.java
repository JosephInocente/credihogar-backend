package com.abarrotes.sistema.inventario.domain.port;

import com.abarrotes.sistema.inventario.domain.model.Inventario;
import com.abarrotes.sistema.inventario.domain.model.MovimientoInventario;
import com.abarrotes.sistema.inventario.domain.model.Ubicacion;

import java.util.Optional;

public interface InventarioRepositoryPort {
    
    // Gestión de ubicaciones
    Ubicacion guardarUbicacion(Ubicacion ubicacion);
    Optional<Ubicacion> buscarUbicacionPorId(Long id);
    
    // Gestión de stock
    Optional<Inventario> buscarStock(Long ubicacionId, Long presentacionId);
    Inventario guardarStock(Inventario inventario);
    
    // Historial inmutable
    MovimientoInventario registrarMovimiento(MovimientoInventario movimiento);
}