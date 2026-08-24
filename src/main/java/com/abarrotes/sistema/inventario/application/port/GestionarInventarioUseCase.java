package com.abarrotes.sistema.inventario.application.port;

import com.abarrotes.sistema.inventario.domain.model.TipoUbicacion;
import com.abarrotes.sistema.inventario.domain.model.Ubicacion;

public interface GestionarInventarioUseCase {
    
    // Para registrar el almacén principal o los carros
    Ubicacion registrarUbicacion(String nombre, TipoUbicacion tipo);
    
    // Para registrar la entrada de mercadería al almacén
    void registrarEntrada(Long ubicacionDestinoId, Long presentacionId, Integer cantidad, String usuario, String motivo);
}