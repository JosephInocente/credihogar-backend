package com.abarrotes.sistema.cliente.application.port;

import com.abarrotes.sistema.cliente.domain.model.Cliente;
import java.util.List;
import java.util.Optional;

public interface GestionarClienteUseCase {
    
    Cliente registrarCliente(Cliente cliente);
    
    List<Cliente> listarClientes();
    
    Optional<Cliente> buscarPorDocumento(String numeroDocumento);
}