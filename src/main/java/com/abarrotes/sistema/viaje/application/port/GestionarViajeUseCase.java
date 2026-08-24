package com.abarrotes.sistema.viaje.application.port;

import com.abarrotes.sistema.viaje.domain.model.DetalleViaje;
import com.abarrotes.sistema.viaje.domain.model.Viaje;
import java.util.List;

public interface GestionarViajeUseCase {
    
    Viaje crearViaje(Viaje viaje);
    
    // NUEVO: Agrega la lista de productos al viaje
    Viaje agregarCarga(Long viajeId, List<DetalleViaje> detalles);
    
    // NUEVO: Confirma la carga, cambia el estado y descuenta el stock del almacén
    void confirmarCargaYDescontarStock(Long viajeId, String usuario);
    
 // NUEVO: Liquida el viaje, devuelve el stock sobrante y libera el vehículo
    void liquidarViaje(Long viajeId, String usuario);
}
