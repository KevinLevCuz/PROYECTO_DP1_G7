package com.dp1code.routing.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Bloqueo {
    private ArrayList<Nodo> nodos;
    private LocalDateTime inicio;
    private LocalDateTime fin;

    public Bloqueo() {
        this.nodos = new ArrayList<>();
    }

    public Bloqueo(ArrayList<Nodo> nodos, LocalDateTime inicio, LocalDateTime fin) {
        this.nodos = new ArrayList<>(nodos);
        this.inicio = inicio;
        this.fin = fin;
    }

    public ArrayList<Nodo> getNodos() { return nodos; }
    public void setNodos(ArrayList<Nodo> nodos) { this.nodos = nodos; }

    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }

    public LocalDateTime getFin() { return fin; }
    public void setFin(LocalDateTime fin) { this.fin = fin; }
}