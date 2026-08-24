package com.abarrotes.sistema.venta.infrastructure.adapter;

import com.abarrotes.sistema.venta.infrastructure.entity.VentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataVentaRepository extends JpaRepository<VentaEntity, Long> {
    
    List<VentaEntity> findByViajeId(Long viajeId);
    
}