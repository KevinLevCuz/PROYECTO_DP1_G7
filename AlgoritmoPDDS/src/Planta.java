public class Planta {
    private static int contadorId = 1;
    private int id;
    private String tipo;
    private double glpMaxima;
    private double glpRest;
    private Nodo ubicacion;
    private double glpRestSim;

    public Planta(String tipo, Nodo ubicacion) {
        this.id = contadorId++;
        this.tipo = tipo;
        AsignarGlpPorTipo(tipo);
        this.ubicacion = ubicacion;
    }
    
    private void AsignarGlpPorTipo(String tipo) {
        switch (tipo) {
            case "PRINCIPAL":
                this.glpMaxima = 10000;
                this.glpRest=10000;
                this.glpRestSim=10000;
                break;
            case "SECUNDARIA":
                this.glpMaxima = 60.0;
                this.glpRest=60.0;
                this.glpRestSim=60;
                break;
            default:
                System.out.println("Ingreso mal algún tipo de Planta.");
                break;
        }
    }
    public Planta crearCopiaSimulacion() {
        Planta copia = new Planta(this.tipo, this.ubicacion);
        copia.setGlpMaxima(this.glpMaxima);
        copia.setGlpRest(this.glpRest);
        copia.setGlpRestSim(this.glpRestSim);
        return copia;
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
     public double getGlpRestSim() {
        return glpRestSim;
    }
    public void setGlpRestSim(double glpRestSim) {
        this.glpRestSim = glpRestSim;
    }
    public void resetSimulacion() {
        this.glpRestSim = this.glpRest;
    }
}
