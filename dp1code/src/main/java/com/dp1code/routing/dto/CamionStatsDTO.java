package com.dp1code.routing.dto;

public class CamionStatsDTO {
    private String codigo;
    private int totalEntregados;
    private double porcentajeGlobal;

    // Getters y setters
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public int getTotalEntregados() {
        return totalEntregados;
    }

    public void setTotalEntregados(int totalEntregados) {
        this.totalEntregados = totalEntregados;
    }

    public double getPorcentajeGlobal() {
        return porcentajeGlobal;
    }

    public void setPorcentajeGlobal(double porcentajeGlobal) {
        this.porcentajeGlobal = porcentajeGlobal;
    }
}
