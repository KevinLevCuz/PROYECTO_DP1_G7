package Algoritmos.SA2;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Ruta {
    private Camion camion;
    private ArrayList<Nodo> camino;
    private ArrayList<Pedido> paradas;
    private LocalDateTime horaSalida;

    public Ruta() {
        this.camino = new ArrayList<>();
        this.paradas = new ArrayList<>();
    }

    public Ruta(Camion camion, ArrayList<Nodo> camino, ArrayList<Pedido> paradas, LocalDateTime horaSalida) {
        this.camion = camion;
        this.camino = camino;
        this.paradas = paradas;
        this.horaSalida = horaSalida;
    }

    public Camion getCamion() { return camion; }
    public void setCamion(Camion camion) { this.camion = camion; }

    public ArrayList<Nodo> getCamino() { return camino; }
    public void setCamino(ArrayList<Nodo> camino) { this.camino = camino; }

    public ArrayList<Pedido> getParadas() { return paradas; }
    public void setParadas(ArrayList<Pedido> paradas) { this.paradas = paradas; }

    public LocalDateTime getHoraSalida() { return horaSalida; }
    public void setHoraSalida(LocalDateTime horaSalida) { this.horaSalida = horaSalida; }
}