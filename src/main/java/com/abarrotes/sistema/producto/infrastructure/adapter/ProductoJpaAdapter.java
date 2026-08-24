package com.abarrotes.sistema.producto.infrastructure.adapter;

import com.abarrotes.sistema.producto.domain.model.Presentacion;
import com.abarrotes.sistema.producto.domain.model.Producto;
import com.abarrotes.sistema.producto.domain.port.ProductoRepositoryPort;
import com.abarrotes.sistema.producto.infrastructure.entity.PresentacionEntity;
import com.abarrotes.sistema.producto.infrastructure.entity.ProductoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductoJpaAdapter implements ProductoRepositoryPort {

    private final SpringDataProductoRepository repository;

    @Override
    public Producto guardar(Producto producto) {
        ProductoEntity entity = mapearAEntity(producto);
        ProductoEntity guardado = repository.save(entity);
        return mapearADominio(guardado);
    }

    @Override
    public Optional<Producto> buscarPorId(Long id) {
        return repository.findById(id).map(this::mapearADominio);
    }

    @Override
    public Optional<Producto> buscarPorSku(String sku) {
        return repository.findBySku(sku).map(this::mapearADominio);
    }

    @Override
    public boolean existePorSku(String sku) {
        return repository.existsBySku(sku);
    }

    @Override
    public List<Producto> listarTodos() {
        return repository.findAll().stream()
                .map(this::mapearADominio)
                .collect(Collectors.toList());
    }

    // --- Métodos de Mapeo ---

    private ProductoEntity mapearAEntity(Producto dominio) {
        ProductoEntity entity = ProductoEntity.builder()
                .id(dominio.getId())
                .sku(dominio.getSku())
                .nombre(dominio.getNombre())
                .categoria(dominio.getCategoria())
                .marca(dominio.getMarca())
                .estado(dominio.getEstado())
                // La lista vacía se crea por el @Builder.Default, la llenamos abajo
                .build();

        // Mapear presentaciones (y establecer la relación bidireccional)
        if (dominio.getPresentaciones() != null) {
            for (Presentacion presDominio : dominio.getPresentaciones()) {
                PresentacionEntity presEntity = PresentacionEntity.builder()
                        .id(presDominio.getId())
                        .nombre(presDominio.getNombre())
                        .unidadBase(presDominio.getUnidadBase())
                        .factorConversion(presDominio.getFactorConversion())
                        .codigoBarras(presDominio.getCodigoBarras())
                        .precioCompraReferencial(presDominio.getPrecioCompraReferencial())
                        .precioVenta(presDominio.getPrecioVenta())
                        .build();
                // Usamos el método utilitario de la entidad para amarrar la relación
                entity.addPresentacion(presEntity); 
            }
        }
        return entity;
    }

    private Producto mapearADominio(ProductoEntity entity) {
        Producto dominio = Producto.builder()
                .id(entity.getId())
                .sku(entity.getSku())
                .nombre(entity.getNombre())
                .categoria(entity.getCategoria())
                .marca(entity.getMarca())
                .estado(entity.getEstado())
                .build();

        if (entity.getPresentaciones() != null) {
            List<Presentacion> presentacionesDominio = entity.getPresentaciones().stream()
                    .map(presEntity -> Presentacion.builder()
                            .id(presEntity.getId())
                            .productoId(entity.getId())
                            .nombre(presEntity.getNombre())
                            .unidadBase(presEntity.getUnidadBase())
                            .factorConversion(presEntity.getFactorConversion())
                            .codigoBarras(presEntity.getCodigoBarras())
                            .precioCompraReferencial(presEntity.getPrecioCompraReferencial())
                            .precioVenta(presEntity.getPrecioVenta())
                            .build())
                    .collect(Collectors.toList());
            dominio.setPresentaciones(presentacionesDominio);
        }
        return dominio;
    }
}