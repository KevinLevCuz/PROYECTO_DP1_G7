package com.dp1code.routing.dto;

public class PedidoArchivoDTO {

    private String tiempoSimulacion;   // Ej: "01d00h04m"
    private int posX;                  // Posición X
    private int posY;                  // Posición Y
    private String idCliente;          // Ej: "c-109"
    private double cantidadGlp;        // Ej: 3.3
    private int plazoHoras;            // Ej: 14

    public String getTiempoSimulacion() {
        return tiempoSimulacion;
    }

    public void setTiempoSimulacion(String tiempoSimulacion) {
        this.tiempoSimulacion = tiempoSimulacion;
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

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public double getCantidadGlp() {
        return cantidadGlp;
    }

    public void setCantidadGlp(double cantidadGlp) {
        this.cantidadGlp = cantidadGlp;
    }

    public int getPlazoHoras() {
        return plazoHoras;
    }

    public void setPlazoHoras(int plazoHoras) {
        this.plazoHoras = plazoHoras;
    }

    @Override
    public String toString() {
        return "PedidoArchivoDTO{" +
                "tiempoSimulacion='" + tiempoSimulacion + '\'' +
                ", posX=" + posX +
                ", posY=" + posY +
                ", idCliente='" + idCliente + '\'' +
                ", cantidadGlp=" + cantidadGlp +
                ", plazoHoras=" + plazoHoras +
                '}';
    }
}
