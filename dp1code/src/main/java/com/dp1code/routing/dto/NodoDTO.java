package com.dp1code.routing.dto;

public class NodoDTO {
    private int posX;
    private int posY;
    private boolean bloqueado;

    public NodoDTO() {}

    public NodoDTO(int posX, int posY, boolean bloqueado) {
        this.posX = posX;
        this.posY = posY;
        this.bloqueado = bloqueado;
    }

    public int getPosX() { return posX; }
    public void setPosX(int posX) { this.posX = posX; }

    public int getPosY() { return posY; }
    public void setPosY(int posY) { this.posY = posY; }

    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }
}
