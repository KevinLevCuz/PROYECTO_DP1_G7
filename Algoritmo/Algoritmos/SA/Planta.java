package Algoritmos.SA2;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

public class Planta {
    private String id;
    private Nodo ubicacion;
    private double capacidadMaxima;
    private double glpDisponible;
    private LocalDateTime siguienteRecarga;
    private LocalDateTime intervaloRecarga;

    public Planta() {}

    public Planta(String id, Nodo ubicacion, double capacidadMaxima, double glpDisponible,
                  LocalDateTime siguienteRecarga, LocalDateTime intervaloRecarga) {
        this.id = id;
        this.ubicacion = ubicacion;
        this.capacidadMaxima = capacidadMaxima;
        this.glpDisponible = glpDisponible;
        this.siguienteRecarga = siguienteRecarga;
        this.intervaloRecarga = intervaloRecarga;
    }

    public Planta(String id, Nodo ubicacion) {
        this.id = id;
        this.ubicacion = ubicacion;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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
