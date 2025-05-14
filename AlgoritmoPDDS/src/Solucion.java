import java.util.List;

public class Solucion {
    private static int contadorId;
    private int id;
    private List<Asignacion> solucion;
    private double fitness;

    public Solucion(List<Asignacion> solucion) {
        this.id = contadorId++;
        this.solucion = solucion;
    }

    public int getId() {
        return id;
    }

    public List<Asignacion> getSolucion() {
        return solucion;
    }

    public void setSolucion(List<Asignacion> solucion) {
        this.solucion = solucion;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }


}
