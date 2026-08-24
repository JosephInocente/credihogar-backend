package com.abarrotes.sistema.viaje.application.service;

import com.abarrotes.sistema.inventario.domain.model.Inventario;
import com.abarrotes.sistema.inventario.domain.model.MovimientoInventario;
import com.abarrotes.sistema.inventario.domain.model.TipoMovimiento;
import com.abarrotes.sistema.inventario.domain.port.InventarioRepositoryPort;
import com.abarrotes.sistema.vehiculo.domain.model.EstadoVehiculo;
import com.abarrotes.sistema.vehiculo.domain.model.Vehiculo;
import com.abarrotes.sistema.vehiculo.domain.port.VehiculoRepositoryPort;
import com.abarrotes.sistema.venta.domain.model.DetalleVenta;
import com.abarrotes.sistema.venta.domain.model.EstadoVenta;
import com.abarrotes.sistema.venta.domain.model.Venta;
import com.abarrotes.sistema.venta.domain.port.VentaRepositoryPort;
import com.abarrotes.sistema.viaje.application.port.GestionarViajeUseCase;
import com.abarrotes.sistema.viaje.domain.model.DetalleViaje;
import com.abarrotes.sistema.viaje.domain.model.EstadoViaje;
import com.abarrotes.sistema.viaje.domain.model.Viaje;
import com.abarrotes.sistema.viaje.domain.port.ViajeRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GestionarViajeService implements GestionarViajeUseCase {

    private final ViajeRepositoryPort viajeRepository;
    private final VehiculoRepositoryPort vehiculoRepository;
    private final InventarioRepositoryPort inventarioRepository;
    
    // NUEVO: Inyectamos el puerto de ventas para poder leer las boletas de la ruta
    private final VentaRepositoryPort ventaRepository;

    @Override
    @Transactional
    public Viaje crearViaje(Viaje viaje) {
        Vehiculo vehiculo = vehiculoRepository.buscarPorId(viaje.getVehiculoId())
                .orElseThrow(() -> new IllegalArgumentException("El vehículo seleccionado no existe"));

        if (!vehiculo.isDisponibleParaViaje()) {
            throw new IllegalStateException("El vehículo seleccionado no se encuentra DISPONIBLE");
        }
        if (viajeRepository.existeViajeActivoParaVehiculo(viaje.getVehiculoId())) {
            throw new IllegalStateException("El vehículo ya está operando en un viaje en curso");
        }
        if (viajeRepository.existeViajeActivoParaTrabajador(viaje.getTrabajadorId())) {
            throw new IllegalStateException("El trabajador ya se encuentra asignado a un viaje en curso");
        }

        vehiculo.marcarComoAsignado();
        vehiculoRepository.guardar(vehiculo);

        viaje.setEstado(EstadoViaje.BORRADOR);
        return viajeRepository.guardar(viaje);
    }

    @Override
    @Transactional
    public Viaje agregarCarga(Long viajeId, List<DetalleViaje> detalles) {
        Viaje viaje = viajeRepository.buscarPorId(viajeId)
                .orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado"));

        if (viaje.getEstado() != EstadoViaje.BORRADOR) {
            throw new IllegalStateException("Solo se puede modificar la carga si el viaje está en BORRADOR");
        }

        detalles.forEach(viaje::agregarDetalle);
        return viajeRepository.guardar(viaje);
    }

    @Override
    @Transactional
    public void confirmarCargaYDescontarStock(Long viajeId, String usuario) {
        Viaje viaje = viajeRepository.buscarPorId(viajeId)
                .orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado"));

        if (viaje.getEstado() != EstadoViaje.BORRADOR) {
            throw new IllegalStateException("El viaje ya fue procesado o no está en BORRADOR");
        }
        if (viaje.getDetalles().isEmpty()) {
            throw new IllegalStateException("No se puede confirmar un viaje sin carga");
        }

        for (DetalleViaje detalle : viaje.getDetalles()) {
            Inventario stock = inventarioRepository.buscarStock(viaje.getAlmacenOrigenId(), detalle.getPresentacionId())
                    .orElseThrow(() -> new IllegalArgumentException("No hay registro de stock para el producto"));

            stock.restar(detalle.getCantidad());
            inventarioRepository.guardarStock(stock);

            MovimientoInventario movimiento = MovimientoInventario.builder()
                    .ubicacionOrigenId(viaje.getAlmacenOrigenId())
                    .ubicacionDestinoId(null) 
                    .presentacionId(detalle.getPresentacionId())
                    .tipo(TipoMovimiento.TRANSFERENCIA)
                    .cantidad(detalle.getCantidad())
                    .fecha(LocalDateTime.now())
                    .usuarioResponsable(usuario)
                    .motivo("Carga para Viaje ID: " + viaje.getId())
                    .build();
            inventarioRepository.registrarMovimiento(movimiento);
        }

        viaje.setEstado(EstadoViaje.CARGADO);
        viajeRepository.guardar(viaje);
    }

    // ==============================================================================
    // NUEVO: LA GRAN LIQUIDACIÓN
    // ==============================================================================
    @Override
    @Transactional // Todo ocurre en bloque: o se liquida perfecto, o no se liquida nada
    public void liquidarViaje(Long viajeId, String usuario) {
        
        Viaje viaje = viajeRepository.buscarPorId(viajeId)
                .orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado"));

        if (viaje.getEstado() != EstadoViaje.EN_RUTA && viaje.getEstado() != EstadoViaje.CARGADO) {
            throw new IllegalStateException("Solo se pueden liquidar viajes que estén EN_RUTA o CARGADOS");
        }

        // 1. Obtener todas las ventas realizadas en este viaje
        List<Venta> ventasDelViaje = ventaRepository.listarPorViaje(viajeId);

        // 2. Sumar cuántas unidades se vendieron de cada producto
        Map<Long, Integer> totalVendidoPorProducto = new HashMap<>();
        for (Venta venta : ventasDelViaje) {
            if (venta.getEstado() == EstadoVenta.EMITIDA) { // Solo contamos ventas válidas
                for (DetalleVenta dv : venta.getDetalles()) {
                    totalVendidoPorProducto.merge(dv.getPresentacionId(), dv.getCantidad(), Integer::sum);
                }
            }
        }

        // 3. Comparar con la Carga Inicial y devolver lo sobrante al Almacén
        for (DetalleViaje carga : viaje.getDetalles()) {
            int cantidadCargada = carga.getCantidad();
            int cantidadVendida = totalVendidoPorProducto.getOrDefault(carga.getPresentacionId(), 0);
            int cantidadSobrante = cantidadCargada - cantidadVendida;

            if (cantidadSobrante > 0) {
                // Devolvemos el stock al inventario original
                Inventario stock = inventarioRepository.buscarStock(viaje.getAlmacenOrigenId(), carga.getPresentacionId())
                        .orElseThrow(() -> new IllegalStateException("Error crítico: Stock base no encontrado para la devolución"));
                
                stock.sumar(cantidadSobrante);
                inventarioRepository.guardarStock(stock);

                // Registramos el movimiento (El famoso Ledger)
                MovimientoInventario movimientoDevolucion = MovimientoInventario.builder()
                        .ubicacionOrigenId(null) // Porque viene de la calle
                        .ubicacionDestinoId(viaje.getAlmacenOrigenId())
                        .presentacionId(carga.getPresentacionId())
                        .tipo(TipoMovimiento.ENTRADA)
                        .cantidad(cantidadSobrante)
                        .fecha(LocalDateTime.now())
                        .usuarioResponsable(usuario)
                        .motivo("Liquidación y sobrante del Viaje ID: " + viaje.getId())
                        .build();
                inventarioRepository.registrarMovimiento(movimientoDevolucion);
            }
        }

        // 4. Liberar el camión para que pueda ser usado mañana
        Vehiculo vehiculo = vehiculoRepository.buscarPorId(viaje.getVehiculoId()).orElseThrow();
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculoRepository.guardar(vehiculo);

        // 5. Cerrar el viaje definitivamente
        viaje.setEstado(EstadoViaje.CERRADO);
        viajeRepository.guardar(viaje);
    }
}