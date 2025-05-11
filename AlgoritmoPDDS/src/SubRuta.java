import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class SubRuta {
    private Nodo ubicacionInicio;
    private Nodo ubicacionFin;
    private LocalDateTime fechaPartida;
    private LocalDateTime fechaLlegada;
    private List<Nodo> trayectoria;

    public SubRuta(Nodo ubicacionInicio, Nodo ubicacionFin) {
        this.ubicacionInicio = ubicacionInicio;
        this.ubicacionFin = ubicacionFin;
        this.trayectoria = new ArrayList<>();
    }
    public List<Nodo> generarTrayectoria(Grid grid, LocalDateTime fechaSimulada) {
        PriorityQueue<Nodo> openList = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Set<Nodo> closedSet = new HashSet<>();

        Nodo start = grid.getNodoAt(ubicacionInicio.getPosX(), ubicacionInicio.getPosY());
        Nodo end = grid.getNodoAt(ubicacionFin.getPosX(), ubicacionFin.getPosY());


        // Reset de valores antes de reutilizar nodos
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                Nodo n = grid.getNodoAt(x, y);
                n.g = Double.POSITIVE_INFINITY;
                n.h = 0;
                n.f = 0;
                n.parent = null;
            }
        }

        start.g = 0;
        start.h = heuristic(start, end);
        start.f = start.h;
        openList.add(start);
        
        while (!openList.isEmpty()) {
            Nodo actual = openList.poll();
            
            if (actual.getPosX() == end.getPosX() && actual.getPosY()== end.getPosY()) {
                return backtrace(actual);
            }

            closedSet.add(actual);

            for (Nodo vecino : grid.getNeighbors(actual)) {

                if (vecino.isBlockedAt(fechaSimulada) || closedSet.contains(vecino)) {
                    continue;
                }

                double tentativeG = actual.g + 1; 

                if (tentativeG < vecino.g) {
                    vecino.parent = actual;
                    vecino.g = tentativeG;
                    vecino.h = heuristic(vecino, end);
                    vecino.f = vecino.g + vecino.h;

                    if (!openList.contains(vecino)) {
                        openList.add(vecino);
                    }

                }
            }
        }

        return new ArrayList<>(); 
    }

    private double heuristic(Nodo a, Nodo b) {
        return Math.abs(a.posX - b.posX) + Math.abs(a.posY - b.posY); 
    }

    private List<Nodo> backtrace(Nodo Nodo) {
        List<Nodo> path = new ArrayList<>();
        while (Nodo != null) {
            path.add(0, Nodo);
            Nodo = Nodo.parent;
        }
        return path;
    }
       public Nodo getUbicacionInicio() {
        return ubicacionInicio;
    }
    public void setUbicacionInicio(Nodo ubicacionInicio) {
        this.ubicacionInicio = ubicacionInicio;
    }
    public Nodo getUbicacionFin() {
        return ubicacionFin;
    }
    public void setUbicacionFin(Nodo ubicacionFin) {
        this.ubicacionFin = ubicacionFin;
    }
    public LocalDateTime getFechaPartida() {
        return fechaPartida;
    }
    public void setFechaPartida(LocalDateTime fechaPartida) {
        this.fechaPartida = fechaPartida;
    }
    public LocalDateTime getFechaLlegada() {
        return fechaLlegada;
    }
    public void setFechaLlegada(LocalDateTime fechaLlegada) {
        this.fechaLlegada = fechaLlegada;
    }
    public List<Nodo> getTrayectoria() {
        return trayectoria;
    }
    public void setTrayectoria(List<Nodo> trayectoria) {
        this.trayectoria = trayectoria;
    }

}
