package com.abarrotes.sistema.inventario.infrastructure.adapter;

import com.abarrotes.sistema.inventario.domain.model.Inventario;
import com.abarrotes.sistema.inventario.domain.model.MovimientoInventario;
import com.abarrotes.sistema.inventario.domain.model.Ubicacion;
import com.abarrotes.sistema.inventario.domain.port.InventarioRepositoryPort;
import com.abarrotes.sistema.inventario.infrastructure.entity.InventarioEntity;
import com.abarrotes.sistema.inventario.infrastructure.entity.MovimientoInventarioEntity;
import com.abarrotes.sistema.inventario.infrastructure.entity.UbicacionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InventarioJpaAdapter implements InventarioRepositoryPort {

    private final SpringDataUbicacionRepository ubicacionRepository;
    private final SpringDataInventarioRepository inventarioRepository;
    private final SpringDataMovimientoRepository movimientoRepository;

    @Override
    public Ubicacion guardarUbicacion(Ubicacion ubicacion) {
        UbicacionEntity entity = UbicacionEntity.builder()
                .id(ubicacion.getId())
                .tipo(ubicacion.getTipo())
                .nombre(ubicacion.getNombre())
                .build();
        UbicacionEntity guardada = ubicacionRepository.save(entity);
        return Ubicacion.builder()
                .id(guardada.getId())
                .tipo(guardada.getTipo())
                .nombre(guardada.getNombre())
                .build();
    }

    @Override
    public Optional<Ubicacion> buscarUbicacionPorId(Long id) {
        return ubicacionRepository.findById(id).map(entity -> Ubicacion.builder()
                .id(entity.getId())
                .tipo(entity.getTipo())
                .nombre(entity.getNombre())
                .build());
    }

    @Override
    public Optional<Inventario> buscarStock(Long ubicacionId, Long presentacionId) {
        return inventarioRepository.findByUbicacionIdAndPresentacionId(ubicacionId, presentacionId)
                .map(entity -> Inventario.builder()
                        .id(entity.getId())
                        .ubicacionId(entity.getUbicacionId())
                        .presentacionId(entity.getPresentacionId())
                        .cantidad(entity.getCantidad())
                        .build());
    }

    @Override
    public Inventario guardarStock(Inventario inventario) {
        InventarioEntity entity = InventarioEntity.builder()
                .id(inventario.getId())
                .ubicacionId(inventario.getUbicacionId())
                .presentacionId(inventario.getPresentacionId())
                .cantidad(inventario.getCantidad())
                .build();
        InventarioEntity guardado = inventarioRepository.save(entity);
        inventario.setId(guardado.getId());
        return inventario;
    }

    @Override
    public MovimientoInventario registrarMovimiento(MovimientoInventario movimiento) {
        MovimientoInventarioEntity entity = MovimientoInventarioEntity.builder()
                .ubicacionOrigenId(movimiento.getUbicacionOrigenId())
                .ubicacionDestinoId(movimiento.getUbicacionDestinoId())
                .presentacionId(movimiento.getPresentacionId())
                .tipo(movimiento.getTipo())
                .cantidad(movimiento.getCantidad())
                .fecha(movimiento.getFecha())
                .usuarioResponsable(movimiento.getUsuarioResponsable())
                .motivo(movimiento.getMotivo())
                .build();
        MovimientoInventarioEntity guardado = movimientoRepository.save(entity);
        movimiento.setId(guardado.getId());
        return movimiento;
    }
}
