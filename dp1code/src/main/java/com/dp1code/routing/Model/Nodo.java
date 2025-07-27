package com.dp1code.routing.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Nodo {
    private String id;
    private int posX;
    private int posY;
    private boolean bloqueado;

    @JsonIgnore
    public double g = Double.MAX_VALUE;

    @JsonIgnore
    public double h = 0;

    @JsonIgnore
    public double f = 0;

    @JsonIgnore
    public Nodo parent = null;

    @JsonIgnore
    public List<TimeRange> bloqueos = new ArrayList<>();

    public Nodo() {}

    public Nodo(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
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

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nodo nodo = (Nodo) o;
        return posX == nodo.posX && posY == nodo.posY;
    }

    @Override
    public int hashCode() {
        return Objects.hash(posX, posY);
    }

    public synchronized void agregarBloqueo(LocalDateTime inicio, LocalDateTime fin) {
        this.bloqueos.add(new TimeRange(inicio, fin));
    }


    public synchronized boolean isBlockedAt(LocalDateTime dateTime) {
        for (TimeRange r : bloqueos) {
            if (r.contains(dateTime)) {
                return true;
            }
        }
        return false;
    }

    public int segundosParaProximoCambio(LocalDateTime fechaSimulada) {
    LocalDateTime proximoCambio = null;

    for (TimeRange bloqueo : bloqueos) {
        LocalDateTime inicio = bloqueo.getStart();
        LocalDateTime fin = bloqueo.getEnd();

        if (inicio.isAfter(fechaSimulada)) {
            if (proximoCambio == null || inicio.isBefore(proximoCambio)) {
                proximoCambio = inicio;
            }
        }

        if (fin.isAfter(fechaSimulada)) {
            if (proximoCambio == null || fin.isBefore(proximoCambio)) {
                proximoCambio = fin;
            }
        }
    }

    if (proximoCambio == null) {
        return 0;
    }

    return (int) Duration.between(fechaSimulada, proximoCambio).toSeconds();
}

public synchronized boolean isBlockedBetween(LocalDateTime inicio, LocalDateTime fin) {
    System.out.println("Inicio:"+ inicio+" Fin:"+fin+" y los bloqueos son: ");
    for (TimeRange bloqueo : this.bloqueos) {
        System.out.println("El bloqueo es: "+bloqueo.getStart()+" "+bloqueo.getEnd());
        if(!bloqueo.getStart().isAfter(inicio) && !bloqueo.getEnd().isBefore(fin)) {
            System.out.println("Ingreso al true");
            return true;
        }
    }
    return false;
}



}
