package com.dp1code.routing.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.cglib.core.Local;

public class Camion {
    private String codigo;
    private String tipo;
    private double pesoVacio;
    private Nodo ubicacionActual;
    private double capacidadMaxima;

    private double glpActual;
    private double glpActualSim;
    private String estado;
    

    private double glpTanque;
    private double glpTanqueSim;

    private boolean enRuta;
    private LocalDateTime disponibleDesde;
    private LocalDateTime horaLibre;   // instante en que terminará la subruta en curso
    private List<SubRuta> subRutasExistentes;
    private List<TimeRange> mantenimientos;

    private int numPedidosAtendidos;

    public int getNumPedidosAtendidos(){
        return numPedidosAtendidos;
    }

    public void setNumPedidosAtendidos(int numPedidosAtendidos){
        this.numPedidosAtendidos = numPedidosAtendidos;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getPesoVacio() {
        return pesoVacio;
    }

    public void setPesoVacio(double pesoVacio) {
        this.pesoVacio = pesoVacio;
    }
    public List<TimeRange> getMantenimientos() {
        return mantenimientos;
    }
    public double getGlpTanque() {
        return glpTanque;
    }

    public void setGlpTanque(double glpTanque) {
        this.glpTanque = glpTanque;
    }

    public void setMantenimientos(List<TimeRange> mantenimientos) {
        this.mantenimientos = mantenimientos;
    }
    public double getGlpActualSim() {
        return glpActualSim;
    }

    public void setGlpActualSim(double glpActualSim) {
        this.glpActualSim = glpActualSim;
    }
    public double getGlpTanqueSim() {
        return glpTanqueSim;
    }

    public void setGlpTanqueSim(double glpTanqueSim) {
        this.glpTanqueSim = glpTanqueSim;
    }

    public Camion() {
        this.numPedidosAtendidos = 0;
    }

    public Camion(String codigo, String tipo, Nodo ubicacionActual, boolean enRuta, LocalDateTime disponibleDesde, double glpTanque, double glpActual) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.ubicacionActual = ubicacionActual;
        this.enRuta = enRuta;
        this.disponibleDesde = disponibleDesde;
        this.glpTanque=glpTanque;
        this.glpTanqueSim=glpTanque;
        this.glpActualSim = glpActual;
        this.glpActual = glpActual;
        AsignarCaracteristicasFlota(tipo);
        this.numPedidosAtendidos = 0;
    }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado=estado;}

    private void AsignarCaracteristicasFlota(String tipo) {
        switch (tipo) {
            case "TA":
                this.pesoVacio=2.5;
                this.capacidadMaxima=25;
                break;
            case "TB":
                this.pesoVacio=2;
                this.capacidadMaxima=15;
                break;
            case "TC":
                this.pesoVacio=1.5;
                this.capacidadMaxima=10;
                break;
            case "TD":
                this.pesoVacio=1;
                this.capacidadMaxima=05;
                break;
            default:
                System.out.println("Se ha ingresado mal los tipos de camiones.");
        }
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

    public boolean isDisponiblePorMantenimiento(LocalDateTime fechaHora) {
        if (this.mantenimientos != null) {
            for (TimeRange mantenimiento : this.mantenimientos) {
                if (mantenimiento.contains(fechaHora)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    public double calcularConsumo(double distanciaKm) {
        double pesoTotal = this.pesoVacio + (this.glpActual * 0.5); 
        return distanciaKm * pesoTotal / 180;
    }

    public double calcularConsumo(double distanciaKm, double glpActual) {
        double pesoTotal = this.pesoVacio + (glpActual * 0.5); 
        return distanciaKm * pesoTotal / 180;
    }

    public boolean alcanzaParaRetornar(Grid grid, Camion c, Nodo ubiActualCamion, double glpActual, double glpTanque, LocalDateTime tiempo){
        Map.Entry<ArrayList<Nodo>, LocalDateTime> resultado = PathFinder.generarTrayectoria(
                        grid, ubiActualCamion, grid.getNodoAt(12, 8), tiempo, tiempo.plusMonths(6), tiempo.plusMinutes(15), tiempo.plusMinutes(15),tiempo.plusMinutes(15), c);

        ArrayList<Nodo> trayectoria = resultado.getKey();
        double glpAConsumir = c.calcularConsumo(trayectoria.size()-1, glpActual);

        if(glpTanque<glpAConsumir){
            return false;
        }

        return true;
    }
    @Override
public Camion clone() {
    Camion clon = new Camion();
    
    clon.setCodigo(this.codigo);
    clon.setTipo(this.tipo);
    clon.setPesoVacio(this.pesoVacio);
    clon.setCapacidadMaxima(this.capacidadMaxima);
    
    // Nodo se considera inmutable por tus equals/hashCode, pero puedes clonarlo si necesitas
    clon.setUbicacionActual(this.ubicacionActual); // si planeas mutar este nodo, mejor clónalo

    clon.setGlpActual(this.glpActual);
    clon.setGlpTanque(this.glpTanque);
    clon.setGlpActualSim(this.glpActualSim);
    clon.setGlpTanqueSim(this.glpTanqueSim);

    clon.setEnRuta(this.enRuta);
    clon.setDisponibleDesde(this.disponibleDesde);
    clon.setHoraLibre(this.horaLibre);

    if (this.mantenimientos != null) {
        List<TimeRange> copiaMantenimientos = new ArrayList<>();
        for (TimeRange tr : this.mantenimientos) {
            copiaMantenimientos.add(tr.clone()); // asegúrate de tener clone() en TimeRange o implementa copia
        }
        clon.setMantenimientos(copiaMantenimientos);
    }

    if (this.subRutasExistentes != null) {
        List<SubRuta> copiaSubRutas = new ArrayList<>();
        for (SubRuta sr : this.subRutasExistentes) {
            copiaSubRutas.add(sr.clone()); // asegúrate de tener clone() en SubRuta
        }
        clon.setSubRutasExistentes(copiaSubRutas);
    }

    return clon;
}

    

}
