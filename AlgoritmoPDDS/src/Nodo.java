public class Nodo {
    private int posX;
    private int posY;
    private boolean bloqueado;
    
    public Nodo(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
        this.bloqueado = false;
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
    public boolean isBloqueado() {
        return bloqueado;
    }
    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }
    public String detallarEnString(){
        return "("+posX+","+posY+")";
    }
}
