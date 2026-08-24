package com.abarrotes.sistema.cliente.application.service;

import com.abarrotes.sistema.cliente.application.port.GestionarClienteUseCase;
import com.abarrotes.sistema.cliente.domain.model.Cliente;
import com.abarrotes.sistema.cliente.domain.port.ClienteRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GestionarClienteService implements GestionarClienteUseCase {

    private final ClienteRepositoryPort clienteRepository;

    @Override
    public Cliente registrarCliente(Cliente cliente) {
        
        // Regla de negocio: El documento debe ser único en el sistema
        if (clienteRepository.existePorDocumento(cliente.getNumeroDocumento())) {
            throw new IllegalArgumentException("Ya existe un cliente registrado con el documento: " + cliente.getNumeroDocumento());
        }

        return clienteRepository.guardar(cliente);
    }

    @Override
    public List<Cliente> listarClientes() {
        return clienteRepository.listarTodos();
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String numeroDocumento) {
        return clienteRepository.buscarPorDocumento(numeroDocumento);
    }
}
