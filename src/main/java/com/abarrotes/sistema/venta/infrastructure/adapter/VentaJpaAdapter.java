package com.abarrotes.sistema.venta.infrastructure.adapter;

import com.abarrotes.sistema.venta.domain.model.DetalleVenta;
import com.abarrotes.sistema.venta.domain.model.Venta;
import com.abarrotes.sistema.venta.domain.port.VentaRepositoryPort;
import com.abarrotes.sistema.venta.infrastructure.entity.DetalleVentaEntity;
import com.abarrotes.sistema.venta.infrastructure.entity.VentaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class VentaJpaAdapter implements VentaRepositoryPort {

    private final SpringDataVentaRepository repository;

    @Override
    public Venta guardar(Venta venta) {
        VentaEntity entity = mapearAEntity(venta);
        VentaEntity guardada = repository.save(entity);
        return mapearADominio(guardada);
    }

    @Override
    public Optional<Venta> buscarPorId(Long id) {
        return repository.findById(id).map(this::mapearADominio);
    }

    @Override
    public List<Venta> listarPorViaje(Long viajeId) {
        return repository.findByViajeId(viajeId).stream()
                .map(this::mapearADominio)
                .collect(Collectors.toList());
    }

    // --- Métodos Privados de Mapeo ---

    private VentaEntity mapearAEntity(Venta venta) {
        VentaEntity entity = VentaEntity.builder()
                .id(venta.getId())
                .clienteId(venta.getClienteId())
                .trabajadorId(venta.getTrabajadorId())
                .viajeId(venta.getViajeId())
                .fechaHora(venta.getFechaHora())
                .estado(venta.getEstado())
                .total(venta.getTotal())
                .build();

        if (venta.getDetalles() != null) {
            for (DetalleVenta detDominio : venta.getDetalles()) {
                DetalleVentaEntity detEntity = DetalleVentaEntity.builder()
                        .id(detDominio.getId())
                        .presentacionId(detDominio.getPresentacionId())
                        .cantidad(detDominio.getCantidad())
                        .precioUnitario(detDominio.getPrecioUnitario())
                        .build();
                entity.addDetalle(detEntity);
            }
        }
        return entity;
    }

    private Venta mapearADominio(VentaEntity entity) {
        Venta venta = Venta.builder()
                .id(entity.getId())
                .clienteId(entity.getClienteId())
                .trabajadorId(entity.getTrabajadorId())
                .viajeId(entity.getViajeId())
                .fechaHora(entity.getFechaHora())
                .estado(entity.getEstado())
                .total(entity.getTotal())
                .build();

        if (entity.getDetalles() != null) {
            entity.getDetalles().forEach(detEntity -> 
                venta.agregarDetalle(DetalleVenta.builder()
                        .id(detEntity.getId())
                        .presentacionId(detEntity.getPresentacionId())
                        .cantidad(detEntity.getCantidad())
                        .precioUnitario(detEntity.getPrecioUnitario())
                        .build())
            );
        }
        return venta;
    }
}