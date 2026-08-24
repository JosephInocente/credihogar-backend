package com.abarrotes.sistema.cliente.infrastructure.adapter;

import com.abarrotes.sistema.cliente.domain.model.Cliente;
import com.abarrotes.sistema.cliente.domain.port.ClienteRepositoryPort;
import com.abarrotes.sistema.cliente.infrastructure.entity.ClienteEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ClienteJpaAdapter implements ClienteRepositoryPort {

    private final SpringDataClienteRepository repository;

    @Override
    public Cliente guardar(Cliente cliente) {
        ClienteEntity entity = mapearAEntity(cliente);
        ClienteEntity guardado = repository.save(entity);
        return mapearADominio(guardado);
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return repository.findById(id).map(this::mapearADominio);
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String numeroDocumento) {
        return repository.findByNumeroDocumento(numeroDocumento).map(this::mapearADominio);
    }

    @Override
    public boolean existePorDocumento(String numeroDocumento) {
        return repository.existsByNumeroDocumento(numeroDocumento);
    }

    @Override
    public List<Cliente> listarTodos() {
        return repository.findAll().stream()
                .map(this::mapearADominio)
                .collect(Collectors.toList());
    }

    // --- Métodos de Mapeo ---

    private ClienteEntity mapearAEntity(Cliente cliente) {
        return ClienteEntity.builder()
                .id(cliente.getId())
                .tipoDocumento(cliente.getTipoDocumento())
                .numeroDocumento(cliente.getNumeroDocumento())
                .razonSocial(cliente.getRazonSocial())
                .direccion(cliente.getDireccion())
                .telefono(cliente.getTelefono())
                .build();
    }

    private Cliente mapearADominio(ClienteEntity entity) {
        return Cliente.builder()
                .id(entity.getId())
                .tipoDocumento(entity.getTipoDocumento())
                .numeroDocumento(entity.getNumeroDocumento())
                .razonSocial(entity.getRazonSocial())
                .direccion(entity.getDireccion())
                .telefono(entity.getTelefono())
                .build();
    }
}