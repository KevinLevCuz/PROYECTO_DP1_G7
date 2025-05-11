public class Planta {
    private static int contadorId = 1;
    private int id;
    private String tipo;
    private double glpMaxima;
    private double glpRest;
    private Nodo ubicacion;

    public Planta(String tipo, Nodo ubicacion) {
        this.id = contadorId++;
        AsignarGlpPorTipo(tipo);
        this.ubicacion = ubicacion;
    }
    
    private void AsignarGlpPorTipo(String tipo) {
        switch (tipo) {
            case "PRINCIPAL":
                this.glpMaxima = 100000.0;
                this.glpRest=100000.0;
                break;
            case "SECUNDARIA":
                this.glpMaxima = 60.0;
                this.glpRest=60.0;
                break;
            default:
                System.out.println("Ingreso mal algún tipo de Planta.");
                break;
        }
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    public double getGlpMaxima() {
        return glpMaxima;
    }
    public void setGlpMaxima(double glpMaxima) {
        this.glpMaxima = glpMaxima;
    }
    public double getGlpRest() {
        return glpRest;
    }
    public void setGlpRest(double glpRest) {
        this.glpRest = glpRest;
    }
    public Nodo getUbicacion() {
        return ubicacion;
    }
    public void setUbicacion(Nodo ubicacion) {
        this.ubicacion = ubicacion;
    }
}
