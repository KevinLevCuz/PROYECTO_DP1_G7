import java.time.LocalDateTime;
import java.util.List;

public class Ruta {
    private Camion camion;
    private List<Nodo> nodos;
    private List<Nodo> nodosParada;
    private LocalDateTime fechaPartida;

    public Ruta(){}    
    public Ruta(Camion camion, List<Nodo> nodos, LocalDateTime fechaPartida) {
        this.camion = camion;
        this.nodos = nodos;
        this.fechaPartida = fechaPartida;
    }
    public Camion getCamion() {
        return camion;
    }
    public void setCamion(Camion camion) {
        this.camion = camion;
    }
    public List<Nodo> getNodos() {
        return nodos;
    }
    public void setNodos(List<Nodo> nodos) {
        this.nodos = nodos;
    }
    public List<Nodo> getNodosParada() {
        return nodosParada;
    }
    public void setNodosParada(List<Nodo> nodosParada) {
        this.nodosParada = nodosParada;
    }
    public LocalDateTime getFechaPartida() {
        return fechaPartida;
    }
    public void setFechaPartida(LocalDateTime fechaPartida) {
        this.fechaPartida = fechaPartida;
    }
    public String detallarNodos(){
        String cadenaNodos="";
        for(Nodo nodo: nodos){
            cadenaNodos += nodo.detallarEnString() + " ";
        }
        return cadenaNodos;
    }
}
