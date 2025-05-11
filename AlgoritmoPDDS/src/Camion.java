import java.time.LocalDateTime;
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

    public Camion(String tipo, Nodo ubicacion) {
        this.tipo = tipo;
        this.ubicacion = ubicacion;
        this.glpTanque=25;
        this.glpTanqueRest=25;
        this.disponible=true;
        AsignarCaracteristicasFlota(tipo);
    }

    private void AsignarCaracteristicasFlota(String tipo) {
        switch (tipo) {
            case "TA":
                this.codigo = tipo+String.valueOf(contadorTipo[0]++);
                this.pesoVacio=2.5;
                this.glpCarga=25;
                this.glpCargaRest=25;
                break;
            case "TB":
                this.codigo = tipo+String.valueOf(contadorTipo[1]++);
                this.pesoVacio=2;
                this.glpCarga=15;
                this.glpCargaRest=15;
                break;
            case "TC":
                this.codigo = tipo+String.valueOf(contadorTipo[2]++);
                this.pesoVacio=1.5;
                this.glpCarga=10;
                this.glpCargaRest=10;
                break;
            case "TD":
                this.codigo = tipo+String.valueOf(contadorTipo[3]++);
                this.pesoVacio=1;
                this.glpCarga=05;
                this.glpCargaRest=05;
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
    public boolean isDisponible() {
        return this.disponible;
    }
    public boolean isDisponible(LocalDateTime fecha, List<Mantenimiento> mantenimientos) {
        return disponible && mantenimientos.stream().noneMatch(m -> 
            fecha.isAfter(m.getInicio()) && fecha.isBefore(m.getFin()));
    }
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
