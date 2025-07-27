package com.dp1code.routing.dto;

public class PedidoResumenDTO {
    private int totalPedidos;
    private int entregados;
    private int pendientes;
    private double promedioGlpPorPedido;
    private double porcentajeCumplimiento;
    private double promedioTiempoEntregaMin;
    private double totalGlpEntregado;

    public PedidoResumenDTO() {
    }

    public int getTotalPedidos() {
        return totalPedidos;
    }

    public void setTotalPedidos(int totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    public int getEntregados() {
        return entregados;
    }

    public void setEntregados(int entregados) {
        this.entregados = entregados;
    }

    public int getPendientes() {
        return pendientes;
    }

    public void setPendientes(int pendientes) {
        this.pendientes = pendientes;
    }

    public double getPromedioGlpPorPedido() {
        return promedioGlpPorPedido;
    }

    public void setPromedioGlpPorPedido(double promedioGlpPorPedido) {
        this.promedioGlpPorPedido = promedioGlpPorPedido;
    }

    public double getPorcentajeCumplimiento() {
        return porcentajeCumplimiento;
    }

    public void setPorcentajeCumplimiento(double porcentajeCumplimiento) {
        this.porcentajeCumplimiento = porcentajeCumplimiento;
    }

    public double getPromedioTiempoEntregaMin() {
        return promedioTiempoEntregaMin;
    }

    public void setPromedioTiempoEntregaMin(double promedioTiempoEntregaMin) {
        this.promedioTiempoEntregaMin = promedioTiempoEntregaMin;
    }

    public double getTotalGlpEntregado() {
        return totalGlpEntregado;
    }

    public void setTotalGlpEntregado(double totalGlpEntregado) {
        this.totalGlpEntregado = totalGlpEntregado;
    }
}
