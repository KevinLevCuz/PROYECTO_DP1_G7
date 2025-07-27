package com.dp1code.routing.dto;

import java.time.LocalDateTime;

public class CamionDTO {
    private String codigo;
    private String tipo;
    private double pesoVacio;
    private int ubicacionNodoId;
    private double capacidadMaxima;
    private double glpActual;
    private double glpTanque;
    private boolean enRuta;
    private LocalDateTime disponibleDesde;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public double getPesoVacio() { return pesoVacio; }
    public void setPesoVacio(double pesoVacio) { this.pesoVacio = pesoVacio; }

    public int getUbicacionNodoId() { return ubicacionNodoId; }
    public void setUbicacionNodoId(int ubicacionNodoId) { this.ubicacionNodoId = ubicacionNodoId; }

    public double getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(double capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    public double getGlpActual() { return glpActual; }
    public void setGlpActual(double glpActual) { this.glpActual = glpActual; }

    public double getGlpTanque() { return glpTanque; }
    public void setGlpTanque(double glpTanque) { this.glpTanque = glpTanque; }

    public boolean isEnRuta() { return enRuta; }
    public void setEnRuta(boolean enRuta) { this.enRuta = enRuta; }

    public LocalDateTime getDisponibleDesde() { return disponibleDesde; }
    public void setDisponibleDesde(LocalDateTime disponibleDesde) { this.disponibleDesde = disponibleDesde; }
}
