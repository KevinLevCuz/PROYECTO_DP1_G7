package com.dp1code.routing.Model;

import java.time.LocalDateTime;
import java.util.List;

public class Camion {
    private String codigo;
    private Nodo ubicacionActual;
    private double capacidadMaxima;
    private double glpActual;
    private boolean enRuta;
    private LocalDateTime disponibleDesde;
    private LocalDateTime horaLibre;   // instante en que terminará la subruta en curso
    private List<SubRuta> subRutasExistentes;

    public Camion() {}

    public Camion(String codigo, Nodo ubicacionActual, double capacidadMaxima,
                  double glpActual, boolean enRuta, LocalDateTime disponibleDesde) {
        this.codigo = codigo;
        this.ubicacionActual = ubicacionActual;
        this.capacidadMaxima = capacidadMaxima;
        this.glpActual = glpActual;
        this.enRuta = enRuta;
        this.disponibleDesde = disponibleDesde;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public Nodo getUbicacionActual() { return ubicacionActual; }
    public void setUbicacionActual(Nodo ubicacionActual) { this.ubicacionActual = ubicacionActual; }

    public double getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(double capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    public double getGlpActual() { return glpActual; }
    public void setGlpActual(double glpActual) { this.glpActual = glpActual; }

    public boolean isEnRuta() { return enRuta; }
    public void setEnRuta(boolean enRuta) { this.enRuta = enRuta; }

    public LocalDateTime getDisponibleDesde() { return disponibleDesde; }
    public void setDisponibleDesde(LocalDateTime disponibleDesde) { this.disponibleDesde = disponibleDesde; }

    public LocalDateTime getHoraLibre() { return horaLibre; }
    public void setHoraLibre(LocalDateTime h) { this.horaLibre = h; }

    public List<SubRuta> getSubRutasExistentes() { return subRutasExistentes; }
    public void setSubRutasExistentes(List<SubRuta> s) { this.subRutasExistentes = s; }
}
