package com.dp1code.routing.dto;

import java.time.LocalDateTime;

public class PlantaDTO {
    private int id;
    private String tipo;
    private int ubicacionId;
    private double capacidadMaxima;
    private double glpDisponible;
    private LocalDateTime siguienteRecarga;
    private LocalDateTime intervaloRecarga;

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getUbicacionId() { return ubicacionId; }
    public void setUbicacionId(int ubicacionId) { this.ubicacionId = ubicacionId; }

    public double getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(double capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    public double getGlpDisponible() { return glpDisponible; }
    public void setGlpDisponible(double glpDisponible) { this.glpDisponible = glpDisponible; }

    public LocalDateTime getSiguienteRecarga() { return siguienteRecarga; }
    public void setSiguienteRecarga(LocalDateTime siguienteRecarga) { this.siguienteRecarga = siguienteRecarga; }

    public LocalDateTime getIntervaloRecarga() { return intervaloRecarga; }
    public void setIntervaloRecarga(LocalDateTime intervaloRecarga) { this.intervaloRecarga = intervaloRecarga; }
}
