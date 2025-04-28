package Algoritmos.Genetico;

public class Cliente {
    int id;
    String identificadorCliente;


    public Cliente(){}

    public Cliente(int id, String identificadorCliente){
        this.id=id;
        this.identificadorCliente=identificadorCliente;
    }

    public int getId() {
        return id;
    }

    public String getIdentificadorCliente() {
        return identificadorCliente;
    }

    public void setIdentificadorCliente(String identificadorCliente) {
        this.identificadorCliente = identificadorCliente;
    }
}