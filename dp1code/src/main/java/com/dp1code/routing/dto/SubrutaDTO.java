package com.dp1code.routing.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SubrutaDTO {
    private int inicioPosX;
    private int inicioPosY;
    private int finPosX;
    private int finPosY;
    private String pedidoId;
    private List<int[]> trayectoria; // cada nodo como [posX, posY]
    private LocalDateTime horaInicio;
    private LocalDateTime horaFin;

    public int getInicioPosX() { return inicioPosX; }
    public void setInicioPosX(int inicioPosX) { this.inicioPosX = inicioPosX; }

    public int getInicioPosY() { return inicioPosY; }
    public void setInicioPosY(int inicioPosY) { this.inicioPosY = inicioPosY; }

    public int getFinPosX() { return finPosX; }
    public void setFinPosX(int finPosX) { this.finPosX = finPosX; }

    public int getFinPosY() { return finPosY; }
    public void setFinPosY(int finPosY) { this.finPosY = finPosY; }

    public String getPedidoId() { return pedidoId; }
    public void setPedidoId(String pedidoId) { this.pedidoId = pedidoId; }

    public List<int[]> getTrayectoria() { return trayectoria; }
    public void setTrayectoria(List<int[]> trayectoria) { this.trayectoria = trayectoria; }

    public LocalDateTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalDateTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalDateTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalDateTime horaFin) { this.horaFin = horaFin; }
}
