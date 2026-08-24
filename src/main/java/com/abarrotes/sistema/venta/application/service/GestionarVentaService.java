package com.abarrotes.sistema.venta.application.service;

import com.abarrotes.sistema.venta.application.port.GestionarVentaUseCase;
import com.abarrotes.sistema.venta.domain.model.EstadoVenta;
import com.abarrotes.sistema.venta.domain.model.Venta;
import com.abarrotes.sistema.venta.domain.port.VentaRepositoryPort;
import com.abarrotes.sistema.viaje.domain.model.EstadoViaje;
import com.abarrotes.sistema.viaje.domain.model.Viaje;
import com.abarrotes.sistema.viaje.domain.port.ViajeRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GestionarVentaService implements GestionarVentaUseCase {

    private final VentaRepositoryPort ventaRepository;
    
    // Inyectamos el repositorio de viajes para poder validar y actualizar el estado de la ruta
    private final ViajeRepositoryPort viajeRepository;

    @Override
    @Transactional // Si falla la venta, no se actualiza el viaje y viceversa
    public Venta registrarVenta(Venta venta) {
        
        // 1. Validar que el viaje exista
        Viaje viaje = viajeRepository.buscarPorId(venta.getViajeId())
                .orElseThrow(() -> new IllegalArgumentException("El viaje especificado no existe"));

        // 2. Regla de Negocio: El viaje debe estar en condiciones de vender
        if (viaje.getEstado() != EstadoViaje.CARGADO && viaje.getEstado() != EstadoViaje.EN_RUTA) {
            throw new IllegalStateException("Solo se pueden registrar ventas en viajes CARGADOS o EN_RUTA");
        }

        // 3. Si es la primera venta, el viaje pasa automáticamente a estar EN_RUTA
        if (viaje.getEstado() == EstadoViaje.CARGADO) {
            viaje.setEstado(EstadoViaje.EN_RUTA);
            viajeRepository.guardar(viaje);
        }

        // 4. Configurar los metadatos de la venta
        venta.setFechaHora(LocalDateTime.now());
        venta.setEstado(EstadoVenta.EMITIDA);
        
        // 5. Recalcular el total por seguridad (confiamos en el backend, no en el frontend)
        venta.calcularTotal();
        if (venta.getTotal().doubleValue() <= 0) {
            throw new IllegalArgumentException("El total de la venta debe ser mayor a cero");
        }

        // 6. Guardar la transacción en la base de datos
        return ventaRepository.guardar(venta);
    }

    @Override
    public List<Venta> listarVentasPorViaje(Long viajeId) {
        return ventaRepository.listarPorViaje(viajeId);
    }
}