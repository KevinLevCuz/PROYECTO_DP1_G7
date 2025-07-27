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
    private boolean entregado;
    private LocalDateTime horaSiguientePedido;
    private String sigId;

    private LocalDateTime tiempoEntrega;

    public LocalDateTime getTiempoEntrega() { return tiempoEntrega; }
    public void setTiempoEntrega(LocalDateTime tiempoEntrega) { this.tiempoEntrega = tiempoEntrega;}


    public boolean isEntregado() {
        return this.entregado;
    }

    public void setEntregado(boolean entregado) {
        this.entregado = entregado;
    }
    private String idCliente;

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

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
        this.entregado = false; 
    }

    public Pedido(String id, Nodo destino, String idCliente, double cantidadGlp, LocalDateTime horaPedido, LocalDateTime plazoMaximoEntrega) {
        this.id=id;
        this.destino = destino;
        this.idCliente = idCliente;
        this.cantidadGlp = cantidadGlp;
        this.horaPedido = horaPedido;
        this.plazoMaximoEntrega = plazoMaximoEntrega;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSigId() { return sigId; }
    public void setSigId(String sigId) { this.sigId = sigId; }

    public Nodo getDestino() { return destino; }
    public void setDestino(Nodo destino) { this.destino = destino; }

    public double getCantidadGlp() { return cantidadGlp; }
    public void setCantidadGlp(double cantidadGlp) { this.cantidadGlp = cantidadGlp; }

    public LocalDateTime getHoraPedido() { return horaPedido; }
    public void setHoraPedido(LocalDateTime horaPedido) { this.horaPedido = horaPedido; }

    public LocalDateTime getHoraSiguientePedido() { return horaSiguientePedido; }
    public void setHoraSiguientePedido(LocalDateTime horaSiguientePedido) { this.horaSiguientePedido = horaSiguientePedido; }

    public LocalDateTime getPlazoMaximoEntrega() { return plazoMaximoEntrega; }
    public void setPlazoMaximoEntrega(LocalDateTime plazoMaximoEntrega) { this.plazoMaximoEntrega = plazoMaximoEntrega; }

    public LocalDateTime getTiempoDescarga() { return tiempoDescarga; }
    public void setTiempoDescarga(LocalDateTime tiempoDescarga) { this.tiempoDescarga = tiempoDescarga; }
}