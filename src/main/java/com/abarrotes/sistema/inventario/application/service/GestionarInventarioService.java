package com.abarrotes.sistema.inventario.application.service;

import com.abarrotes.sistema.inventario.application.port.GestionarInventarioUseCase;
import com.abarrotes.sistema.inventario.domain.model.Inventario;
import com.abarrotes.sistema.inventario.domain.model.MovimientoInventario;
import com.abarrotes.sistema.inventario.domain.model.TipoMovimiento;
import com.abarrotes.sistema.inventario.domain.model.TipoUbicacion;
import com.abarrotes.sistema.inventario.domain.model.Ubicacion;
import com.abarrotes.sistema.inventario.domain.port.InventarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GestionarInventarioService implements GestionarInventarioUseCase {

    private final InventarioRepositoryPort inventarioRepository;

    @Override
    public Ubicacion registrarUbicacion(String nombre, TipoUbicacion tipo) {
        Ubicacion nuevaUbicacion = Ubicacion.builder()
                .nombre(nombre)
                .tipo(tipo)
                .build();
        return inventarioRepository.guardarUbicacion(nuevaUbicacion);
    }

    @Override
    @Transactional // ¡CRUCIAL! Asegura integridad ACID (RF31)
    public void registrarEntrada(Long ubicacionDestinoId, Long presentacionId, Integer cantidad, String usuario, String motivo) {
        
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de entrada debe ser mayor a cero");
        }

        // 1. Validar que la ubicación exista
        Ubicacion destino = inventarioRepository.buscarUbicacionPorId(ubicacionDestinoId)
                .orElseThrow(() -> new IllegalArgumentException("Ubicación destino no encontrada"));

        // 2. Buscar si ya existe stock de esa presentación en esa ubicación. 
        // Si no existe, lo inicializamos en 0 usando el Builder.
        Inventario stockActual = inventarioRepository.buscarStock(ubicacionDestinoId, presentacionId)
                .orElse(Inventario.builder()
                        .ubicacionId(ubicacionDestinoId)
                        .presentacionId(presentacionId)
                        .cantidad(0)
                        .build());

        // 3. Sumar la cantidad al stock (la lógica matemática vive en la entidad pura)
        stockActual.sumar(cantidad);

        // 4. Guardar la "foto actual" del stock
        inventarioRepository.guardarStock(stockActual);

        // 5. Registrar el movimiento inmutable en el historial (Ledger)
        MovimientoInventario movimiento = MovimientoInventario.builder()
                .ubicacionOrigenId(null) // null porque es una entrada externa al sistema
                .ubicacionDestinoId(ubicacionDestinoId)
                .presentacionId(presentacionId)
                .tipo(TipoMovimiento.ENTRADA)
                .cantidad(cantidad)
                .fecha(LocalDateTime.now())
                .usuarioResponsable(usuario)
                .motivo(motivo)
                .build();

        inventarioRepository.registrarMovimiento(movimiento);
    }
}
