package com.abarrotes.sistema.cliente.domain.port;

import com.abarrotes.sistema.cliente.domain.model.Cliente;
import java.util.Optional;

public interface ConsultaDocumentoPort {
    Optional<Cliente> consultarDni(String dni);
    Optional<Cliente> consultarRuc(String ruc);
}