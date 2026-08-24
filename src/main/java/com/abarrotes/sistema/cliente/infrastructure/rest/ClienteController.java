package com.abarrotes.sistema.cliente.infrastructure.rest;

import com.abarrotes.sistema.cliente.application.port.GestionarClienteUseCase;
import com.abarrotes.sistema.cliente.domain.model.Cliente;
import com.abarrotes.sistema.cliente.domain.port.ConsultaDocumentoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final GestionarClienteUseCase gestionarClienteUseCase;
    private final ConsultaDocumentoPort consultaDocumentoPort;

    @PostMapping
    public ResponseEntity<?> registrarCliente(@RequestBody CrearClienteRequest request) {
        try {
            // 1. Mapear del DTO (Web) al Modelo de Dominio (Puro)
            Cliente nuevoCliente = Cliente.builder()
                    .tipoDocumento(request.getTipoDocumento())
                    .numeroDocumento(request.getNumeroDocumento())
                    .razonSocial(request.getRazonSocial())
                    .direccion(request.getDireccion())
                    .telefono(request.getTelefono())
                    .build();

            // 2. Enviar el dominio al Caso de Uso
            Cliente clienteRegistrado = gestionarClienteUseCase.registrarCliente(nuevoCliente);

            // 3. Retornar éxito
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Cliente registrado exitosamente con ID: " + clienteRegistrado.getId());

        } catch (IllegalArgumentException e) {
            // Si el DNI/RUC ya existe, retornamos un error 400
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(gestionarClienteUseCase.listarClientes());
    }

    // Endpoint útil para que el trabajador busque rápidamente a la bodega
    @GetMapping("/documento/{numero}")
    public ResponseEntity<?> buscarPorDocumento(@PathVariable("numero") String numeroDocumento) {
        Optional<Cliente> cliente = gestionarClienteUseCase.buscarPorDocumento(numeroDocumento);
        
        if (cliente.isPresent()) {
            return ResponseEntity.ok(cliente.get());
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrado en el sistema");
    }
    
    // Endpoint para integrarnos con APIs Públicas (SUNAT/RENIEC)
    @GetMapping("/externo/{tipo}/{numero}")
    public ResponseEntity<?> consultarDocumentoExterno(
            @PathVariable("tipo") String tipo, 
            @PathVariable("numero") String numero) {
        
        if (tipo.equalsIgnoreCase("DNI") && numero.length() == 8) {
            Optional<Cliente> clienteExterno = consultaDocumentoPort.consultarDni(numero);
            
            if (clienteExterno.isPresent()) {
                return ResponseEntity.ok(clienteExterno.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("DNI no encontrado o error de conexión con RENIEC");
            }
            
        } else if (tipo.equalsIgnoreCase("RUC") && numero.length() == 11) {
            Optional<Cliente> clienteExterno = consultaDocumentoPort.consultarRuc(numero);
            
            if (clienteExterno.isPresent()) {
                return ResponseEntity.ok(clienteExterno.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("RUC no encontrado o error de conexión con SUNAT");
            }
        }
        
        return ResponseEntity.badRequest().body("Tipo de documento (DNI/RUC) o longitud inválida");
    }
}
