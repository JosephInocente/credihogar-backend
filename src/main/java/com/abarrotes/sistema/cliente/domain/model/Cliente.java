package com.abarrotes.sistema.cliente.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {
    private Long id;
    private String tipoDocumento; // Ej: "DNI", "RUC"
    private String numeroDocumento;
    private String razonSocial;   // Nombre de la persona o de la bodega
    private String direccion;
    private String telefono;
}