package com.abarrotes.sistema.vehiculo.application.port;

import com.abarrotes.sistema.vehiculo.domain.model.Vehiculo;
import java.util.List;

public interface GestionarVehiculoUseCase {
    
    Vehiculo registrarVehiculo(Vehiculo vehiculo);
    
    List<Vehiculo> listarVehiculos();
    
    List<Vehiculo> listarVehiculosDisponibles();
}