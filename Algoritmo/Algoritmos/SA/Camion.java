package Algoritmos.SA;

import java.time.LocalDateTime;

/**
 * Clase que representa un camión cisterna de la flota
 */
public class Camion {
    public enum TipoCamion { TA, TB, TC, TD }
    
    private String codigo; // Ejemplo: "TA01"
    private TipoCamion tipo;
    private double pesoVacio; // Tara en toneladas
    private int capacidadEfectiva; // Capacidad en m³
    private double pesoMaximoCarga; // Peso máximo de carga en toneladas
    private boolean disponible; // Disponibilidad del camión
    private LocalDateTime proximoMantenimiento; // Fecha del próximo mantenimiento

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

    /**
     * Calcula el peso combinado (tara + carga) del camión
     * @param metrosCubicosCargados Cantidad de GLP cargado en m³
     */
    public double calcularPesoCombinado(int metrosCubicosCargados) {
        if (metrosCubicosCargados < 0 || metrosCubicosCargados > capacidadEfectiva) {
            throw new IllegalArgumentException("Cantidad de GLP inválida");
        }
        return pesoVacio + (pesoMaximoCarga * metrosCubicosCargados / capacidadEfectiva);
    }

    /**
     * Calcula el consumo de petróleo para una distancia y carga dada
     * @param distancia Distancia en km
     * @param metrosCubicosCargados Cantidad de GLP cargado en m³
     */
    public double calcularConsumoCombustible(double distancia, int metrosCubicosCargados) {
        double peso = calcularPesoCombinado(metrosCubicosCargados);
        return distancia * peso / 180; // Fórmula según especificación
    }

    /**
     * Verifica si el camión está en mantenimiento en la fecha/hora dada
     */
    public boolean estaEnMantenimiento(LocalDateTime fechaHora) {
        return proximoMantenimiento != null && 
               !fechaHora.isBefore(proximoMantenimiento) && 
               !fechaHora.isAfter(proximoMantenimiento.plusHours(24));
    }

    // Métodos de acceso
    public String getCodigo() { return codigo; }
    public TipoCamion getTipo() { return tipo; }
    public double getPesoVacio() { return pesoVacio; }
    public int getCapacidadEfectiva() { return capacidadEfectiva; }
    public double getPesoMaximoCarga() { return pesoMaximoCarga; }
    public boolean estaDisponible() { return disponible; }
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