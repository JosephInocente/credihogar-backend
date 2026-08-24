package com.abarrotes.sistema.cliente.infrastructure.adapter;

import com.abarrotes.sistema.cliente.infrastructure.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataClienteRepository extends JpaRepository<ClienteEntity, Long> {
    
    Optional<ClienteEntity> findByNumeroDocumento(String numeroDocumento);
    
    boolean existsByNumeroDocumento(String numeroDocumento);
}