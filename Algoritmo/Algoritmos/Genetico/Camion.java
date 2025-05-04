package Algoritmos.Genetico;

import java.time.LocalDateTime;


public class Camion {
    public enum TipoCamion { TA, TB, TC, TD }
    
    private String codigo; 
    private TipoCamion tipo;
    private double pesoVacio; 
    private int capacidadEfectiva; 
    private double pesoMaximoCarga; 
    private boolean disponible; 
    private LocalDateTime proximoMantenimiento; 

    public Camion(){}


    public Camion(String codigo, TipoCamion tipo, double pesoVacio, 
                int capacidadEfectiva, double pesoMaximoCarga) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.pesoVacio = pesoVacio;
        this.capacidadEfectiva = capacidadEfectiva;
        this.pesoMaximoCarga = pesoMaximoCarga;
        this.disponible = true;
    }

    public double calcularPesoCombinado(int metrosCubicosCargados) {
       
        metrosCubicosCargados = Math.max(0, Math.min(metrosCubicosCargados, capacidadEfectiva));
        return pesoVacio + (pesoMaximoCarga * metrosCubicosCargados / capacidadEfectiva);
    }

    
    public double calcularConsumoCombustible(double distancia, int metrosCubicosCargados) {
        double peso = calcularPesoCombinado(metrosCubicosCargados);
        return distancia * peso / 180; // Fórmula según especificación
    }

    
    public boolean estaEnMantenimiento(LocalDateTime fechaHora) {
        return proximoMantenimiento != null && 
               !fechaHora.isBefore(proximoMantenimiento) && 
               !fechaHora.isAfter(proximoMantenimiento.plusHours(24));
    }

    
    public String getCodigo() { return codigo; }
    public TipoCamion getTipo() { return tipo; }
    public double getPesoVacio() { return pesoVacio; }
    public int getCapacidadEfectiva() { return capacidadEfectiva; }
    public double getPesoMaximoCarga() { return pesoMaximoCarga; }
    public boolean getDisponible() { return disponible; }
    public LocalDateTime getProximoMantenimiento() { return proximoMantenimiento; }

    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public void setProximoMantenimiento(LocalDateTime proximoMantenimiento) { 
        this.proximoMantenimiento = proximoMantenimiento; 
    }

    @Override
    public String toString() {
        return String.format("Camión %s (%s) - Capacidad: %dm³ - %s",
                codigo, tipo, capacidadEfectiva, 
                disponible ? "Disponible" : "No disponible");
    }
}