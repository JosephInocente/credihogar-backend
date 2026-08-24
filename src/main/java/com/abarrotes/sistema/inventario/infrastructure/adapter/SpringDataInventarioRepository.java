package com.abarrotes.sistema.inventario.infrastructure.adapter;

import com.abarrotes.sistema.inventario.infrastructure.entity.InventarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataInventarioRepository extends JpaRepository<InventarioEntity, Long> {
    Optional<InventarioEntity> findByUbicacionIdAndPresentacionId(Long ubicacionId, Long presentacionId);
}