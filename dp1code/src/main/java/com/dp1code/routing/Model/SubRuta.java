package com.dp1code.routing.Model;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import com.dp1code.routing.Model.Nodo;

public class SubRuta {
    private Nodo inicio;
    private Nodo fin;
    private Pedido pedido;
    private ArrayList<Nodo> trayectoria;
    private LocalDateTime horaInicio;
    private LocalDateTime horaFin;
    private ArrayList<LocalDateTime> tiemposNodo;

    public SubRuta() {
        this.trayectoria = new ArrayList<>();
    }

    public SubRuta(Nodo inicio, Nodo fin, Pedido pedido,
            ArrayList<Nodo> trayectoria,
            LocalDateTime horaInicio, LocalDateTime horaFin) {
        this.inicio = inicio;
        this.fin = fin;
        this.pedido = pedido;
        this.trayectoria = new ArrayList<>(trayectoria);
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.tiemposNodo = new ArrayList<>(trayectoria.size());
        rellenarTiemposNodo(horaInicio, trayectoria);
    }

    /**
 * Constructor adicional que recibe explícitamente la lista de tiempos por nodo.
 
    public SubRuta(Nodo inicio, Nodo fin, Pedido pedido,
                ArrayList<Nodo> trayectoria, ArrayList<LocalDateTime> tiemposNodo, LocalDateTime horaInicio, LocalDateTime horaFin) {
        this.inicio = inicio;
        this.fin = fin;
        this.pedido = pedido;
        this.trayectoria = new ArrayList<>(trayectoria);
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;

        // Guardamos directamente la lista de tiempos que ya calculaste en el servicio
        this.tiemposNodo = new ArrayList<>(tiemposNodo);
    }*/


    public void rellenarTiemposNodo(LocalDateTime base, ArrayList<Nodo> tray) {
        LocalDateTime t = base;
        tiemposNodo.add(t); // tiempo de llegada al primer nodo (posición 'inicio')

        if (tray.size() < 2) {
            // No hay trayecto suficiente para calcular tiempos, salimos
            this.horaFin = base;
            return;
        }

        for (int i = 1; i < tray.size(); i++) {
            t=t.plusSeconds(72);
            tiemposNodo.add(t);
        }

        this.horaFin = tiemposNodo.get(tiemposNodo.size() - 1);
    }

    public ArrayList<LocalDateTime> getTiemposNodo() {
        return tiemposNodo;
    }

    public Nodo getInicio() {
        return inicio;
    }

    public void setInicio(Nodo inicio) {
        this.inicio = inicio;
    }

    public Nodo getFin() {
        return fin;
    }

    public void setFin(Nodo fin) {
        this.fin = fin;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public ArrayList<Nodo> getTrayectoria() {
        return trayectoria;
    }

    public void setTrayectoria(ArrayList<Nodo> trayectoria) {
        this.trayectoria = trayectoria;
    }

    public LocalDateTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalDateTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalDateTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalDateTime horaFin) {
        this.horaFin = horaFin;
    }
   @Override
    public SubRuta clone() {
        // Clonamos solo la lista de nodos (trayectoria), los nodos como objetos pueden mantenerse si son inmutables
        ArrayList<Nodo> trayectoriaClonada = new ArrayList<>(this.trayectoria);
        
        return new SubRuta(
            this.inicio,
            this.fin,
            this.pedido, // si Pedido es inmutable, esto es seguro
            trayectoriaClonada,
            this.horaInicio,
            this.horaFin
        );
    }


}