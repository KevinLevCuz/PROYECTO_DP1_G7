package Algoritmos.SA2;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class SubRuta {
    private Nodo inicio;
    private Nodo fin;
    private Pedido pedido;
    private ArrayList<Nodo> trayectoria;
    private LocalDateTime horaInicio;
    private LocalDateTime horaFin;

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
    }

    public Nodo getInicio() { return inicio; }
    public void setInicio(Nodo inicio) { this.inicio = inicio; }

    public Nodo getFin() { return fin; }
    public void setFin(Nodo fin) { this.fin = fin; }

    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }

    public ArrayList<Nodo> getTrayectoria() { return trayectoria; }
    public void setTrayectoria(ArrayList<Nodo> trayectoria) { this.trayectoria = trayectoria; }

    public LocalDateTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalDateTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalDateTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalDateTime horaFin) { this.horaFin = horaFin; }
}