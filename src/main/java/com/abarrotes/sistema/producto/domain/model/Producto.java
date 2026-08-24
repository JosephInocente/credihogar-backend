package com.abarrotes.sistema.producto.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {
    private Long id;
    private String sku; // Código único (ej. ACE-001)
    private String nombre;
    private String categoria;
    private String marca;
    private EstadoProducto estado;
    
    // Un producto tiene varias formas de venderse
    @Builder.Default
    private List<Presentacion> presentaciones = new ArrayList<>();
    
    public void agregarPresentacion(Presentacion presentacion) {
        this.presentaciones.add(presentacion);
    }
}