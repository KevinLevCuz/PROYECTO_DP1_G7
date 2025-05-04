package Algoritmos.Genetico;

import java.awt.Point;
import java.time.LocalDateTime;
import java.util.List;

public class Bloqueo {
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private List<Point> nodos;

    public Bloqueo(){}

    public Bloqueo(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, List<Point> nodos) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.nodos = nodos;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public List<Point> getNodos() {
        return nodos;
    }

    public void setNodos(List<Point> nodos) {
        this.nodos = nodos;
    }

    
    public boolean estaBloqueado(Point nodo, LocalDateTime fechaHora) {
        return nodos.contains(nodo) && 
               (fechaHora.isEqual(fechaHoraInicio) || fechaHora.isAfter(fechaHoraInicio)) &&
               (fechaHora.isBefore(fechaHoraFin) || fechaHora.isEqual(fechaHoraFin));
    }

    public boolean estaActivo(LocalDateTime fechaHora) {
        return (fechaHora.isEqual(fechaHoraInicio) || fechaHora.isAfter(fechaHoraInicio)) &&
               (fechaHora.isBefore(fechaHoraFin) || fechaHora.isEqual(fechaHoraFin));
    }

}
