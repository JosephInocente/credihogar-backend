package com.abarrotes.sistema.inventario.infrastructure.adapter;

import com.abarrotes.sistema.inventario.infrastructure.entity.UbicacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUbicacionRepository extends JpaRepository<UbicacionEntity, Long> {
}
