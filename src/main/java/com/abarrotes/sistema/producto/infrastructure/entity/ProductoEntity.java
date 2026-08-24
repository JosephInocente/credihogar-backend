package com.abarrotes.sistema.producto.infrastructure.entity;

import com.abarrotes.sistema.producto.domain.model.EstadoProducto;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private String nombre;

    private String categoria;
    
    private String marca;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoProducto estado;

    // Relación Uno a Muchos: Un producto tiene varias presentaciones
    // CascadeType.ALL significa que si guardamos un producto, también se guardan sus presentaciones
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PresentacionEntity> presentaciones = new ArrayList<>();

    // Método utilitario para mantener la relación bidireccional sincronizada
    public void addPresentacion(PresentacionEntity presentacion) {
        presentaciones.add(presentacion);
        presentacion.setProducto(this);
    }
}