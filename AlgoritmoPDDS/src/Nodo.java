import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Nodo {
    public int posX, posY;
    public double g = Double.MAX_VALUE;
    public double h = 0;
    public double f = 0;
    public Nodo parent = null;
    public List<TimeRange> bloqueos = new ArrayList<>();

    public double getG() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
    }

    public double getF() {
        return f;
    }

    public void setF(double f) {
        this.f = f;
    }

    public List<TimeRange> getBloqueos() {
        return bloqueos;
    }

    public void setBloqueos(List<TimeRange> bloqueos) {
        this.bloqueos = bloqueos;
    }

    public Nodo(int x, int y) {
        this.posX = x;
        this.posY = y;
    }

    public boolean isBlockedAt(LocalDateTime dateTime) {
        for (TimeRange r : bloqueos) {
            if (r.contains(dateTime)) {
                return true;
            }
        }
        return false;
    }
    public String detallarEnString(){
        return "("+posX+","+posY+")";
    }

    public void agregarBloqueo(LocalDateTime inicio, LocalDateTime fin) {
        this.bloqueos.add(new TimeRange(inicio, fin));
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int SegundosParaProximoInicioBloqueo(LocalDateTime fechaSimulada) {
        LocalDateTime proximoBloqueo = null;
        int segundos = 0;
        for (TimeRange bloqueo : bloqueos) {
            LocalDateTime inicioBloqueo = bloqueo.getStart();
            
            if (inicioBloqueo.isAfter(fechaSimulada)) {
                if (proximoBloqueo == null || inicioBloqueo.isBefore(proximoBloqueo)) {
                    proximoBloqueo = inicioBloqueo;
                }
            }
        }
        if(proximoBloqueo==null){
            segundos=0;
        } else{
            segundos = (int) Duration.between(fechaSimulada, proximoBloqueo).toSeconds();
        }        
        return segundos;
    }

}
