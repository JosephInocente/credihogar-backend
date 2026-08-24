package com.abarrotes.sistema.producto.infrastructure.adapter;

import com.abarrotes.sistema.producto.infrastructure.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataProductoRepository extends JpaRepository<ProductoEntity, Long> {
    
    Optional<ProductoEntity> findBySku(String sku);
    
    boolean existsBySku(String sku);
}
