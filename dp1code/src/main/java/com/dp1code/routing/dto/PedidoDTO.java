package com.dp1code.routing.dto;

import java.time.LocalDateTime;

public class PedidoDTO {
    private double cantidadGlp;
    private LocalDateTime horaPedido;
    private LocalDateTime plazoMaximoEntrega;
    private String idCliente;
    private int posX;
    private int posY;

    public PedidoDTO() {}

    // Getters y Setters

    public double getCantidadGlp() {
        return cantidadGlp;
    }

    public void setCantidadGlp(double cantidadGlp) {
        this.cantidadGlp = cantidadGlp;
    }

    public LocalDateTime getHoraPedido() {
        return horaPedido;
    }

    public void setHoraPedido(LocalDateTime horaPedido) {
        this.horaPedido = horaPedido;
    }

    public LocalDateTime getPlazoMaximoEntrega() {
        return plazoMaximoEntrega;
    }

    public void setPlazoMaximoEntrega(LocalDateTime plazoMaximoEntrega) {
        this.plazoMaximoEntrega = plazoMaximoEntrega;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
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
}