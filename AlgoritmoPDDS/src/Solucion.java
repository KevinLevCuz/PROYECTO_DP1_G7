import java.util.List;

public class Solucion {
    private static int contadorId;
    private int id;
    private List<Ruta> solucion;

    public Solucion(List<Ruta> solucion) {
        this.id = contadorId++;
        this.solucion = solucion;
    }

    public int getId() {
        return id;
    }

    public List<Ruta> getSolucion() {
        return solucion;
    }

    public void setSolucion(List<Ruta> solucion) {
        this.solucion = solucion;
    }


}
