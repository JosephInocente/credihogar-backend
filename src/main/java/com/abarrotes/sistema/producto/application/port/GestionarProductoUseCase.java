package com.abarrotes.sistema.producto.application.port;

import com.abarrotes.sistema.producto.domain.model.Producto;
import java.util.List;

public interface GestionarProductoUseCase {
    
    // Recibe un producto armado (con sus presentaciones) y lo procesa
    Producto crearProducto(Producto producto);
    
    List<Producto> listarProductos();
}
