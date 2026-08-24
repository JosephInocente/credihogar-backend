package com.abarrotes.sistema.producto.infrastructure.rest;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CrearProductoRequest {
    private String sku;
    private String nombre;
    private String categoria;
    private String marca;
    private String imagenUrl; // <-- AGREGADO PARA CLOUDINARY
    private List<PresentacionDTO> presentaciones;
}