package com.abarrotes.sistema.venta.application.port;

import com.abarrotes.sistema.venta.domain.model.Venta;
import java.util.List;

public interface GestionarVentaUseCase {
    
    // Registra una nueva boleta/factura en ruta
    Venta registrarVenta(Venta venta);
    
    // Lista todas las ventas de un viaje (Vital para la liquidación final)
    List<Venta> listarVentasPorViaje(Long viajeId);
    
    
}
