package Algoritmos.SA2;

import java.util.Objects;

public class Nodo {
    private int posX;
    private int posY;
    private boolean bloqueado;

    public Nodo() {}

    public Nodo(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
    }

    public int getPosX() { return posX; }
    public void setPosX(int posX) { this.posX = posX; }

    public int getPosY() { return posY; }
    public void setPosY(int posY) { this.posY = posY; }

    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nodo)) return false;
        Nodo n = (Nodo) o;
        return this.posX == n.posX && this.posY == n.posY;
    }

    @Override
    public int hashCode() {
        return Objects.hash(posX, posY);
    }
}