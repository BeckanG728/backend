package com.tpdteam3.backend.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codiProd")
    private Integer codiProd;

    @Column(name = "nombProd")
    private String nombProd;

    @Column(name = "precProd")
    private Double precProd;

    @Column(name = "stocProd")
    private Double stocProd;

    @Version
    private Long version;

    public Integer getCodiProd() {
        return codiProd;
    }

    public void setCodiProd(Integer codiProd) {
        this.codiProd = codiProd;
    }

    public String getNombProd() {
        return nombProd;
    }

    public void setNombProd(String nombProd) {
        this.nombProd = nombProd;
    }

    public Double getPrecProd() {
        return precProd;
    }

    public void setPrecProd(Double precProd) {
        this.precProd = precProd;
    }

    public Double getStocProd() {
        return stocProd;
    }

    public void setStocProd(Double stocProd) {
        this.stocProd = stocProd;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}