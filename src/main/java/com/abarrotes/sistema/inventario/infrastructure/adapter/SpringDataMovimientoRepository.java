package com.abarrotes.sistema.inventario.infrastructure.adapter;

import com.abarrotes.sistema.inventario.infrastructure.entity.MovimientoInventarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataMovimientoRepository extends JpaRepository<MovimientoInventarioEntity, Long> {
}
