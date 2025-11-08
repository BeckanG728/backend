package com.tpdteam3.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "archivo")
public class Archivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String tipo;
    private Long tamaño;

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getTamaño() {
        return tamaño;
    }

    public void setTamaño(Long tamaño) {
        this.tamaño = tamaño;
    }
}
