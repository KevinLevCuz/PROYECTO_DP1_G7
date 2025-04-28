package Algoritmos.Genetico;

public class Pedido {
    int id;
    int dia;
    int cantidad;
    int ubicacionX;
    int ubicacionY;
    int hora;
    Cliente cliente;
    String estado;
    int tiempoMaximo;

    
    // Constructor
    public Pedido(){}
    public Pedido(int id, int dia, int cantidad, int ubicacionX, int ubicacionY, int hora, Cliente cliente) {
        this.id = id;
        this.dia = dia;
        this.cantidad = cantidad;
        this.ubicacionX = ubicacionX;
        this.ubicacionY = ubicacionY;
        this.hora = hora;
        this.cliente=cliente;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getDia() {
        return dia;
    }

    public int getCantidad() {
        return cantidad;
    }

    public int getUbicacionX() {
        return ubicacionX;
    }

    public int getUbicacionY() {
        return ubicacionY;
    }

    public int getHora() {
        return hora;
    }

    public int getTiempoMaximo() {
        return tiempoMaximo;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setDia(int dia) {
        this.dia = dia;
    }



    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setUbicacionX(int ubicacionX) {
        this.ubicacionX = ubicacionX;
    }

    public void setUbicacionY(int ubicacionY) {
        this.ubicacionY = ubicacionY;
    }

    public void setHora(int hora) {
        this.hora = hora;
    }

    public void setTiempoMaximo(int tiempoMaximo) {
        this.tiempoMaximo = tiempoMaximo;
    }

    public void setCliente(Cliente cliente){
        this.cliente=cliente;
    }
    public Cliente getCliente(){
        return this.cliente;
    }
    public void setEstado(String estado){
        this.estado=estado;
    }
    public String getEstado(){
        return this.estado;
    }

}
