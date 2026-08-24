package com.abarrotes.sistema.cliente.infrastructure.adapter;

import com.abarrotes.sistema.cliente.domain.model.Cliente;
import com.abarrotes.sistema.cliente.domain.port.ConsultaDocumentoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApisPeruAdapter implements ConsultaDocumentoPort {

    private final RestTemplate restTemplate;
    
    // Tu token oficial de Decolecta
    private final String API_TOKEN = "sk_18348.tNMVdHOiMDx1rdOQ0BwWC9TISdOQAj9R";

    @Override
    public Optional<Cliente> consultarDni(String dni) {
        try {
            // URL OFICIAL EXACTA SEGÚN LA DOCUMENTACIÓN DE DECOLECTA
            String url = "https://api.decolecta.com/v1/reniec/dni?numero=" + dni;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + API_TOKEN);
            headers.set("Accept", "application/json"); 
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<DniResponseDTO> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, DniResponseDTO.class);

            if (response.getBody() != null) {
                DniResponseDTO dto = response.getBody();
                String nombreCompleto = dto.getNombreCompleto();
                
                if (nombreCompleto != null && !nombreCompleto.isBlank()) {
                    Cliente cliente = Cliente.builder()
                            .tipoDocumento("DNI")
                            .numeroDocumento(dni)
                            .razonSocial(nombreCompleto)
                            .direccion("") // <-- Cambiado a vacío para que el trabajador pueda ingresarla libremente
                            .build();
                    return Optional.of(cliente);
                }
            }
        } catch (HttpClientErrorException e) {
            System.err.println("⚠️ Error HTTP Decolecta (DNI): " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("⚠️ Error inesperado (DNI): " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Cliente> consultarRuc(String ruc) {
        try {
            // URL OFICIAL SUNAT SEGÚN LOS ESTÁNDARES DE DECOLECTA
            String url = "https://api.decolecta.com/v1/sunat/ruc?numero=" + ruc;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + API_TOKEN);
            headers.set("Accept", "application/json"); 
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<RucResponseDTO> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, RucResponseDTO.class);

            if (response.getBody() != null) {
                RucResponseDTO dto = response.getBody();
                Cliente cliente = Cliente.builder()
                        .tipoDocumento("RUC")
                        .numeroDocumento(ruc)
                        .razonSocial(dto.getRazonSocial() != null ? dto.getRazonSocial() : "EMPRESA S.A.C.")
                        .direccion(dto.getDireccion() != null ? dto.getDireccion() : "-")
                        .build();
                return Optional.of(cliente);
            }
        } catch (HttpClientErrorException e) {
            System.err.println("⚠️ Error HTTP Decolecta (RUC): " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("⚠️ Error inesperado (RUC): " + e.getMessage());
        }
        return Optional.empty();
    }
}