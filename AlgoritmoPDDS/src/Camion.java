import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Camion {
    private static  int[] contadorTipo  = new int[4];
    private String codigo;
    private String tipo;
    private double pesoVacio;
    private Nodo ubicacion;
    private double glpTanque;
    private double glpCarga;
    private double glpTanqueRest;
    private double glpCargaRest;
    private boolean disponible;
    private List<TimeRange> mantenimientos;
    private double glpCargaRestSim; 
    private double glpTanqueRestSim;
    private boolean asignacionSimulada;

    public Camion(String tipo, Nodo ubicacion) {
        this.tipo = tipo;
        this.ubicacion = ubicacion;
        this.glpTanque=25;
        this.glpTanqueRest=25;
        this.glpTanqueRestSim=25;
        this.disponible=true;
        this.mantenimientos = new ArrayList<>();
        this.asignacionSimulada = false;
        AsignarCaracteristicasFlota(tipo);
    }

    private void AsignarCaracteristicasFlota(String tipo) {
        switch (tipo) {
            case "TA":
                this.codigo = tipo+String.format("%02d", ++contadorTipo[0]);
                this.pesoVacio=2.5;
                this.glpCarga=25;
                this.glpCargaRest=25;
                this.glpCargaRestSim=25;
                break;
            case "TB":
                this.codigo = tipo+String.format("%02d", ++contadorTipo[1]);
                this.pesoVacio=2;
                this.glpCarga=15;
                this.glpCargaRest=15;
                this.glpCargaRestSim=15;
                break;
            case "TC":
                this.codigo = tipo+String.format("%02d", ++contadorTipo[2]);
                this.pesoVacio=1.5;
                this.glpCarga=10;
                this.glpCargaRest=10;
                this.glpCargaRestSim=10;
                break;
            case "TD":
                this.codigo = tipo+String.format("%02d", ++contadorTipo[3]);
                this.pesoVacio=1;
                this.glpCarga=05;
                this.glpCargaRest=05;
                this.glpCargaRestSim=05;
                break;
            default:
                System.out.println("Se ha ingresado mal los tipos de camiones.");
        }
    }

    public double calcularConsumo(double distanciaKm) {
        double pesoTotal = this.pesoVacio + (this.glpCargaRest * 0.5); 
        return distanciaKm * pesoTotal / 180;
    }
    public boolean tieneCombustibleSuficiente(double distanciaKm) {
        return this.glpTanqueRest >= calcularConsumo(distanciaKm);
    }
    public Camion crearCopiaSimulacion() {
        Camion copia = new Camion(this.tipo, this.ubicacion);
        copia.codigo = this.codigo;
        copia.pesoVacio = this.pesoVacio;
        copia.glpTanque = this.glpTanque;
        copia.glpCarga = this.glpCarga;
        copia.glpTanqueRestSim = this.glpTanqueRest;
        copia.glpCargaRestSim = this.glpCargaRest;
        copia.disponible = this.disponible;
        copia.mantenimientos = new ArrayList<>();
        for (TimeRange m : this.mantenimientos) {
            copia.mantenimientos.add(new TimeRange(m.start,m.end)); 
        }
        return copia;
    }
    public void resetSimulacion() {
        this.glpTanqueRestSim = this.glpTanqueRest;
        this.glpCargaRestSim = this.glpCargaRest;
    }
     public double getGlpCargaRestSim() {
        return glpCargaRestSim;
    }
    public void setGlpCargaRestSim(double glpCargaRestSim) {
        this.glpCargaRestSim = glpCargaRestSim;
    }
    public double getGlpTanqueRestSim() {
        return glpTanqueRestSim;
    }
    public void setGlpTanqueRestSim(double glpTanqueRestSim) {
        this.glpTanqueRestSim = glpTanqueRestSim;
    }
    public String getCodigo() {
        return codigo;
    }
    public String getTipo() {
        return tipo;
    }
    public double getPesoVacio() {
        return pesoVacio;
    }
    public void setPesoVacio(double pesoVacio) {
        this.pesoVacio = pesoVacio;
    }
    public Nodo getUbicacion() {
        return ubicacion;
    }
    public void setUbicacion(Nodo ubicacion) {
        this.ubicacion = ubicacion;
    }
    public double getGlpTanque() {
        return glpTanque;
    }
    public void setGlpTanque(double glpTanque) {
        this.glpTanque = glpTanque;
    }
    public double getGlpCarga() {
        return glpCarga;
    }
    public void setGlpCarga(double glpCarga) {
        this.glpCarga = glpCarga;
    }
    public double getGlpTanqueRest() {
        return glpTanqueRest;
    }
    public void setGlpTanqueRest(double glpTanqueRest) {
        this.glpTanqueRest = glpTanqueRest;
    }
    public double getGlpCargaRest() {
        return glpCargaRest;
    }
    public void setGlpCargaRest(double glpCargaRest) {
        this.glpCargaRest = glpCargaRest;
    }    
    public List<TimeRange> getMantenimientos() {
        return mantenimientos;
    }
    public void setMantenimientos(List<TimeRange> mantenimientos) {
        this.mantenimientos = mantenimientos;
    }
    public boolean isDisponible(LocalDateTime fechaHora) {
        // Primero verificar el estado básico de disponibilidad
        if (!this.disponible || this.asignacionSimulada ) {
            return false;
        }
        // Luego verificar si está en mantenimiento en esa fecha/hora
        if (this.mantenimientos != null) {
            for (TimeRange mantenimiento : this.mantenimientos) {
                if (mantenimiento.contains(fechaHora)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    public boolean estaDisponibleEnRango(LocalDateTime inicio, LocalDateTime fin) {

        if (!this.disponible || this.asignacionSimulada) {
            return false;
        }

        if (this.mantenimientos != null) {
            for (TimeRange mantenimiento : this.mantenimientos) {
                if (!(fin.isBefore(mantenimiento.start) || inicio.isAfter(mantenimiento.end))) {
                    return false;
                }
            }
        }

        return true;
    }
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
    public boolean isAsignacionSimulada() {
        return asignacionSimulada;
    }
    public void setAsignacionSimulada(boolean asignacionSimulada) {
        this.asignacionSimulada = asignacionSimulada;
    }
}
