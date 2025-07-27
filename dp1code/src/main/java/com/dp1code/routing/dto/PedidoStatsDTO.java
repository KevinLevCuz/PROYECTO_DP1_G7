package com.dp1code.routing.dto;

public class PedidoStatsDTO {
    private int id;
    private String horaPedido;
    private String plazoMaximoEntrega;
    private String horaEntrega;
    private int tiempoDisponible;
    private int tiempoReal;
    private double porcentajeUtilizado;

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHoraPedido() {
        return horaPedido;
    }

    public void setHoraPedido(String horaPedido) {
        this.horaPedido = horaPedido;
    }

    public String getPlazoMaximoEntrega() {
        return plazoMaximoEntrega;
    }

    public void setPlazoMaximoEntrega(String plazoMaximoEntrega) {
        this.plazoMaximoEntrega = plazoMaximoEntrega;
    }

    public String getHoraEntrega() {
        return horaEntrega;
    }

    public void setHoraEntrega(String horaEntrega) {
        this.horaEntrega = horaEntrega;
    }

    public int getTiempoDisponible() {
        return tiempoDisponible;
    }

    public void setTiempoDisponible(int tiempoDisponible) {
        this.tiempoDisponible = tiempoDisponible;
    }

    public int getTiempoReal() {
        return tiempoReal;
    }

    public void setTiempoReal(int tiempoReal) {
        this.tiempoReal = tiempoReal;
    }

    public double getPorcentajeUtilizado() {
        return porcentajeUtilizado;
    }

    public void setPorcentajeUtilizado(double porcentajeUtilizado) {
        this.porcentajeUtilizado = porcentajeUtilizado;
    }
}
