import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Utilidades {
    public static int contarPedidosEnRuta(List<SubRuta> subRutas, List<Pedido> pedidos) {
        // Implementar según tu lógica de identificación de pedidos
        int num_pedidos = 0;
        for(SubRuta subRuta: subRutas){
            if(esPedido(subRuta.getUbicacionFin(),pedidos)){
                num_pedidos++;
            }
        }
        return num_pedidos; 
    }
    public static LocalDateTime calcularHoraFin(Asignacion asignacion) {
        return asignacion.getSubRutas().getLast().getFechaLlegada();
    }
    public static int contarParadas(List<SubRuta> subRutas,List<Pedido> pedidos, List<Planta> plantas) {
        // Cada subruta que termina en cliente o planta cuenta como parada
        int contador=0;
        for(SubRuta subRuta: subRutas){
            if(esPedido(subRuta.getUbicacionFin(), pedidos) || esPlanta(subRuta.getUbicacionFin(), plantas)){
                contador++;
            }
        }
        return contador-1;
    }
    public static double calcularDistanciaTotal(Asignacion asignacion) {
        double distancia = 0.0;
        for(SubRuta subRuta: asignacion.getSubRutas()){
            distancia += subRuta.getTrayectoria().size()-1;
        }
        return distancia;
    }
    public static boolean esPlanta(Nodo nodo, List<Planta> plantas) {
        return plantas.stream().anyMatch(p -> p.getUbicacion().equals(nodo));
    }

    public static boolean esPedido(Nodo nodo, List<Pedido> pedidos){
        return pedidos.stream().anyMatch(p -> p.getUbicacion().equals(nodo));
    }

    public static boolean esPlantaPrincipal(Nodo nodo, List<Planta> plantas) {
        return plantas.stream()
            .filter(p -> p.getTipo().equals("PRINCIPAL"))
            .anyMatch(p -> p.getUbicacion().equals(nodo));
    }

    public static Planta obtenerPlanta(Nodo nodo, List<Planta> plantas) {
        return plantas.stream()
            .filter(p -> p.getUbicacion().equals(nodo))
            .findFirst()
            .orElse(null);
    }
    public static Pedido obtenerPedido(Nodo nodo, List<Pedido> pedidos){
        return pedidos.stream()
        .filter(p ->p.getUbicacion().equals(nodo))
        .findFirst()
        .orElse(null);
    }
    public static int calcularDistanciaDeTrayectoria(List<Nodo> trayectoria) {
        /* 
        System.out.println("La trayectoria a utilizar es: ");
        for(Nodo n: trayectoria){
            System.out.println("("+n.getPosX()+","+n.getPosY()+")");
        } */
        return trayectoria.size()-1;
    }
    public static List<Nodo> obtenerNodosIntermedios(int x1, int y1, int x2, int y2, Grid grid) {
        List<Nodo> nodos = new ArrayList<>();
        if (x1 == x2) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                Nodo nodo = grid.getNodoAt(x1, y);
                if (nodo != null) {
                    nodos.add(nodo);
                }
            }
        } else if (y1 == y2) {
            for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
                Nodo nodo = grid.getNodoAt(x, y1);
                if (nodo != null) {
                    nodos.add(nodo);
                }
            }
        } else {
            // Si no es línea recta, puedes lanzar un warning o permitir diagonales si lo deseas
            System.err.println("Advertencia: No se admite bloqueo diagonal entre (" + x1 + "," + y1 + ") y (" + x2 + "," + y2 + ")");
        }
        return nodos;
    }
    public static void mostrarEstadoPlantas(List<Planta> plantas) {
        System.out.println("\n=== ESTADO DE PLANTAS ===");
        System.out.printf("%-12s | %-10s | %s\n", "Planta", "Tipo", "GLP Disponible");
        System.out.println("----------------------------------");
        for (Planta planta : plantas) {
            System.out.printf("%-12s | %-10s | %.1f/%.1f m3\n",
                "Planta " + planta.getId(),
                planta.getTipo(),
                planta.getGlpRest(),
                planta.getGlpMaxima());
        }
    }
}
