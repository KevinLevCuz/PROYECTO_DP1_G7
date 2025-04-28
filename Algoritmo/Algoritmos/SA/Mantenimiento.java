package Algoritmos.SA;

public class Mantenimiento {
    private int idMantenimiento;
    private int anho;
    private int mes;
    private int dia;
    private String codigo;

    public Mantenimiento() {}

    // Getters
    public int getIdMantenimiento() {
        return idMantenimiento;
    }

    public int getAnho() {
        return anho;
    }

    public int getMes() {
        return mes;
    }

    public int getDia() {
        return dia;
    }

    public String getCodigo() {
        return codigo;
    }

    // Setters
    public void setIdMantenimiento(int idMantenimiento) {
        this.idMantenimiento = idMantenimiento;
    }

    public void setAnho(int anho) {
        this.anho = anho;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}