package com.dp1code.routing.Model;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

public class Pedido {
    private String id;
    private Nodo destino;
    private double cantidadGlp;
    private LocalDateTime horaPedido;
    private LocalDateTime plazoMaximoEntrega;
    private LocalDateTime tiempoDescarga;
    private String idCliente;

    public Pedido() {}

    public Pedido(String id, Nodo destino, double cantidadGlp,
                  LocalDateTime horaPedido, LocalDateTime plazoMaximoEntrega,
                  LocalDateTime tiempoDescarga) {
        this.id = id;
        this.destino = destino;
        this.cantidadGlp = cantidadGlp;
        this.horaPedido = horaPedido;
        this.plazoMaximoEntrega = plazoMaximoEntrega;
        this.tiempoDescarga = tiempoDescarga;
    }

    public Pedido(Nodo destino, String idCliente, double cantidadGlp, LocalDateTime horaPedido, LocalDateTime plazoMaximoEntrega) {
        this.destino = destino;
        this.idCliente = idCliente;
        this.cantidadGlp = cantidadGlp;
        this.horaPedido = horaPedido;
        this.plazoMaximoEntrega = plazoMaximoEntrega;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Nodo getDestino() { return destino; }
    public void setDestino(Nodo destino) { this.destino = destino; }

    public double getCantidadGlp() { return cantidadGlp; }
    public void setCantidadGlp(double cantidadGlp) { this.cantidadGlp = cantidadGlp; }

    public LocalDateTime getHoraPedido() { return horaPedido; }
    public void setHoraPedido(LocalDateTime horaPedido) { this.horaPedido = horaPedido; }

    public LocalDateTime getPlazoMaximoEntrega() { return plazoMaximoEntrega; }
    public void setPlazoMaximoEntrega(LocalDateTime plazoMaximoEntrega) { this.plazoMaximoEntrega = plazoMaximoEntrega; }

    public LocalDateTime getTiempoDescarga() { return tiempoDescarga; }
    public void setTiempoDescarga(LocalDateTime tiempoDescarga) { this.tiempoDescarga = tiempoDescarga; }
}