import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Bloqueo {
    private List<Nodo> nodos;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    
    public Bloqueo(List<Nodo> nodos, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        this.nodos = expandirTramos(nodos);
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
    }
    public List<Nodo> getNodos() {
        return nodos;
    }
    public void setNodos(List<Nodo> nodos) {
        this.nodos = nodos;
    }
    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }
    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }
    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }
    public String detallarNodos(){
        String cadenaNodos="";
        for(Nodo nodo: nodos){
            cadenaNodos += nodo.detallarEnString() + " ";
        }
        return cadenaNodos;
    }
    public static List<Nodo> expandirTramos(List<Nodo> nodosExtremos) {
    List<Nodo> nodosCompletos = new ArrayList<>();

    for (int i = 0; i < nodosExtremos.size() - 1; i++) {
        Nodo inicio = nodosExtremos.get(i);
        Nodo fin = nodosExtremos.get(i + 1);

        if (inicio.getPosX() == fin.getPosX()) {
            // Tramo vertical
            int x = inicio.getPosX();
            int yInicio = Math.min(inicio.getPosY(), fin.getPosY());
            int yFin = Math.max(inicio.getPosY(), fin.getPosY());
            for (int y = yInicio; y <= yFin; y++) {
                Nodo nodo = new Nodo(x, y);
                if (!nodosCompletos.contains(nodo)) nodosCompletos.add(nodo);
            }
        } else if (inicio.getPosY() == fin.getPosY()) {
            // Tramo horizontal
            int y = inicio.getPosY();
            int xInicio = Math.min(inicio.getPosX(), fin.getPosX());
            int xFin = Math.max(inicio.getPosX(), fin.getPosX());
            for (int x = xInicio; x <= xFin; x++) {
                Nodo nodo = new Nodo(x, y);
                if (!nodosCompletos.contains(nodo)) nodosCompletos.add(nodo);
            }
        } else {
            throw new IllegalArgumentException("Los tramos deben ser horizontales o verticales.");
        }
    }

    return nodosCompletos;
}

}
