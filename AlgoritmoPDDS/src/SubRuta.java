import java.time.Duration;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DuplicateFormatFlagsException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class SubRuta {
    private Nodo ubicacionInicio;
    private Nodo ubicacionFin;
    private LocalDateTime fechaPartida;
    private LocalDateTime fechaLlegada;
    private List<Nodo> trayectoria;
    private Pedido pedido;

    public SubRuta(Nodo ubicacionInicio, Nodo ubicacionFin, Pedido pedido) {
        this.ubicacionInicio = ubicacionInicio;
        this.ubicacionFin = ubicacionFin;
        this.trayectoria = new ArrayList<>();
        this.pedido = pedido;
    }
    public Map.Entry<List<Nodo>,Integer> generarTrayectoria(Grid grid, LocalDateTime fechaSimulada, LocalDateTime fechaMaxima) {
    
        PriorityQueue<Nodo> openList = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Set<Nodo> closedSet = new HashSet<>();
        int segundos = 0;
        int segundosMinimos = 14401;

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
       // LocalDateTime tiempoActualizadoTemporal = fechaSimulada;
     //   System.out.println("El tiempo actual de fecha Simulada que ingresa es:"+tiempoActualizadoTemporal);

        while (!openList.isEmpty()) {
            Nodo actual = openList.poll();
            
            if (actual.getPosX() == end.getPosX() && actual.getPosY()== end.getPosY()) {
                List<Nodo> rutaFinal = backtrace(actual);
                segundosMinimos = calcularSegundosMinimosRuta(rutaFinal, fechaSimulada);
                return new AbstractMap.SimpleEntry<>(rutaFinal, segundosMinimos);
            }

            closedSet.add(actual);

            for (Nodo vecino : grid.getNeighbors(actual)) {

                int pasosHastaVecino = (int) actual.g + 1;
                LocalDateTime tiempoLlegadaVecino = fechaSimulada.plusSeconds(pasosHastaVecino * 72);

                //System.out.println("Estoy en el nodo: "+actual.detallarEnString()+" y el pasos hasta vecino es: "+pasosHastaVecino);

                if (tiempoLlegadaVecino.isAfter(fechaMaxima)) {
                    //System.out.println("No se alcanza llegar a tiempo al nodo final. Tiempo de llegada: " + tiempoLlegadaVecino + ", fecha máxima: " + fechaMaxima);
                    return new AbstractMap.SimpleEntry<>(new ArrayList<>(), 14401);
                }

                if (vecino.isBlockedAt(tiempoLlegadaVecino) || closedSet.contains(vecino)) {
                    //System.out.println("Ingreso aqui con: "+vecino.isBlockedAt(tiempoActualizadoTemporal) +" y "+ closedSet.contains(vecino));
                    
                    continue;
                }

                double tentativeG = actual.g + 1; 

                if (tentativeG < vecino.g) {
                    vecino.parent = actual;
                    vecino.g = tentativeG;
                    vecino.h = heuristic(vecino, end);
                    vecino.f = vecino.g + vecino.h;

                    
                    if (openList.contains(vecino)) {
                        openList.remove(vecino); 
                    }
                    openList.add(vecino);

                    /*segundos = vecino.SegundosParaProximoInicioBloqueo(tiempoActualizadoTemporal);
                    if(segundos < segundosMinimos && segundos!=0){
                        segundosMinimos = segundos;
                    }

                    
                    if(tiempoActualizadoTemporal.isAfter(fechaMaxima)){
                        System.out.println("El tiempoActualizatemporal es: "+tiempoActualizadoTemporal+" y la fechaMaxima es:"+fechaMaxima+" y el openlist.size es "+openList.size() + " y los nodos son: "+start.detallarEnString()+" y el final es:"+end.detallarEnString());
                        System.out.println("Parece que no se va a alcanzar el tiempo pa llegar. GAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                        return new AbstractMap.SimpleEntry<>(new ArrayList<>(), segundosMinimos);
                        
                    }*/
                }
                
            }
            //tiempoActualizadoTemporal = tiempoActualizadoTemporal.plusSeconds(72);
        }

        return new AbstractMap.SimpleEntry<>(new ArrayList<>(), segundosMinimos);
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
    private int calcularSegundosMinimosRuta(List<Nodo> ruta, LocalDateTime inicio) {
        int segundosMinimos = 14401;
        for (int i = 0; i < ruta.size(); i++) {
            Nodo nodo = ruta.get(i);
            LocalDateTime tiempoLlegada = inicio.plusSeconds(i * 72);
            int segundos = nodo.SegundosParaProximoInicioBloqueo(tiempoLlegada);

            if (segundos != 0 && segundos < segundosMinimos) {
                segundosMinimos = segundos;
            }
        }
        return segundosMinimos;
    }

    public Pedido getPedido() {
        return pedido;
    }
    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
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
