package com.abarrotes.sistema.producto.domain.port;

import com.abarrotes.sistema.producto.domain.model.Producto;
import java.util.Optional;
import java.util.List;

public interface ProductoRepositoryPort {
    Producto guardar(Producto producto);
    Optional<Producto> buscarPorId(Long id);
    Optional<Producto> buscarPorSku(String sku);
    boolean existePorSku(String sku);
    List<Producto> listarTodos();
}