package com.dp1code.routing.Model;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

public class Planta {
    private int id;
    private String tipo;
    private Nodo ubicacion;
    private double capacidadMaxima;
    private double glpDisponible;
    private LocalDateTime siguienteRecarga;
    private LocalDateTime intervaloRecarga;

    public Planta() {}

    public Planta(int id,String tipo, Nodo ubicacion) {
        this.id = id;
        this.tipo = tipo;
        AsignarGlpPorTipo(tipo);
        this.ubicacion = ubicacion;
    }

    private void AsignarGlpPorTipo(String tipo) {
        switch (tipo) {
            case "PRINCIPAL":
                this.capacidadMaxima = 10000;
                this.glpDisponible=10000;
                break;
            case "SECUNDARIA":
                this.capacidadMaxima = 60.0;
                this.glpDisponible=60.0;
                break;
            default:
                System.out.println("Ingreso mal algún tipo de Planta.");
                break;
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Nodo getUbicacion() { return ubicacion; }
    public void setUbicacion(Nodo ubicacion) { this.ubicacion = ubicacion; }

    public double getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(double capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    public double getGlpDisponible() { return glpDisponible; }
    public void setGlpDisponible(double glpDisponible) { this.glpDisponible = glpDisponible; }

    public LocalDateTime getSiguienteRecarga() { return siguienteRecarga; }
    public void setSiguienteRecarga(LocalDateTime siguienteRecarga) { this.siguienteRecarga = siguienteRecarga; }

    public LocalDateTime getIntervaloRecarga() { return intervaloRecarga; }
    public void setIntervaloRecarga(LocalDateTime intervaloRecarga) { this.intervaloRecarga = intervaloRecarga; }
}
