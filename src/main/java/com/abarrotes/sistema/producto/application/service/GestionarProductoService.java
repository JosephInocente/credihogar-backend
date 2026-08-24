package com.abarrotes.sistema.producto.application.service;

import com.abarrotes.sistema.producto.application.port.GestionarProductoUseCase;
import com.abarrotes.sistema.producto.domain.model.EstadoProducto;
import com.abarrotes.sistema.producto.domain.model.Producto;
import com.abarrotes.sistema.producto.domain.port.ProductoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GestionarProductoService implements GestionarProductoUseCase {

    // Inyectamos el puerto de salida para hablar con la base de datos
    private final ProductoRepositoryPort productoRepository;

    @Override
    public Producto crearProducto(Producto producto) {
        
        // 1. Regla de negocio: No se permiten SKU duplicados (HU-05)
        if (productoRepository.existePorSku(producto.getSku())) {
            throw new IllegalArgumentException("Ya existe un producto registrado con el SKU: " + producto.getSku());
        }

        // 2. Establecer estado inicial
        producto.setEstado(EstadoProducto.ACTIVO);

        // 3. Guardar el producto (el adaptador JPA se encargará de guardar también las presentaciones)
        return productoRepository.guardar(producto);
    }

    @Override
    public List<Producto> listarProductos() {
        // En un escenario real con muchos datos, aquí usaríamos paginación (RNF03)
        // Por ahora listaremos todos para probar el flujo.
        return productoRepository.listarTodos();
    }
}
