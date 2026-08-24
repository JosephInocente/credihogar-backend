package com.abarrotes.sistema.cliente.domain.port;

import com.abarrotes.sistema.cliente.domain.model.Cliente;
import java.util.List;
import java.util.Optional;

public interface ClienteRepositoryPort {
    Cliente guardar(Cliente cliente);
    Optional<Cliente> buscarPorId(Long id);
    Optional<Cliente> buscarPorDocumento(String numeroDocumento);
    boolean existePorDocumento(String numeroDocumento);
    List<Cliente> listarTodos();
}