package com.dp1code.routing.dto;

public class CamionResumenDTO {
    private String codigo;
    private int pedidosAtendidos;
    private double glpConsumido;
    private double promedioGlpPorPedido;

    public CamionResumenDTO() {
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public int getPedidosAtendidos() {
        return pedidosAtendidos;
    }

    public void setPedidosAtendidos(int pedidosAtendidos) {
        this.pedidosAtendidos = pedidosAtendidos;
    }

    public double getGlpConsumido() {
        return glpConsumido;
    }

    public void setGlpConsumido(double glpConsumido) {
        this.glpConsumido = glpConsumido;
    }

    public double getPromedioGlpPorPedido() {
        return promedioGlpPorPedido;
    }

    public void setPromedioGlpPorPedido(double promedioGlpPorPedido) {
        this.promedioGlpPorPedido = promedioGlpPorPedido;
    }
}
