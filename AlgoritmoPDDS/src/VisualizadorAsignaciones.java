import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VisualizadorAsignaciones {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public static void mostrarResumenAsignaciones(List<Asignacion> asignaciones, Grid grid, LocalDateTime fechaHora,List<Pedido> pedidos, List<Planta> plantas) {
        System.out.println("\n=== RESUMEN DE ASIGNACIONES ===");
        System.out.printf("%-10s | %-18s | %-18s | %-8s | %s\n", 
            "Camión", "Hora Salida", "Hora Llegada", "N° Pedidos", "Distancia");
        System.out.println("-------------------------------------------------------------------------------");
        
        for (Asignacion asignacion : asignaciones) {
            Camion camion = asignacion.getCamion();
            LocalDateTime fin = Utilidades.calcularHoraFin(asignacion);
            int numPedidos = Utilidades.contarPedidosEnRuta(asignacion.getSubRutas(), pedidos);
            
            System.out.printf("%-10s | %-18s | %-18s | %-10d | %-1f km | %-18s \n",
                camion.getCodigo(),
                asignacion.getFechaPartida().format(formatter),
                fin.format(formatter),
                numPedidos,
                Utilidades.calcularDistanciaTotal(asignacion.getSubRutas(),grid, fechaHora),
                asignacion.getSubRutas().getFirst().getUbicacionFin().detallarEnString());
        }
    }
    
    public static void mostrarDetalleCamion(Asignacion asignacion, Grid grid, LocalDateTime fechaHora,List<Pedido> pedidos, List<Planta> plantas) {
        Camion camion = asignacion.getCamion();
        System.out.println("\n=== DETALLE DE ASIGNACIÓN ===");
        System.out.println("Camión: " + camion.getCodigo() + " (" + camion.getTipo() + ")");
        System.out.println("GLP Tanque: " + camion.getGlpTanqueRest() + "/" + camion.getGlpTanque() + " m3");
        System.out.println("GLP Carga: " + camion.getGlpCargaRest() + "/" + camion.getGlpCarga() + " m3");
        System.out.println("Hora de salida: " + asignacion.getFechaPartida().format(formatter));
        
        System.out.println("\nRuta:");
        int orden = 1;
        double distanciaAcumulada = 0;
        for (SubRuta subRuta : asignacion.getSubRutas()) {
            List<Nodo> trayectoria = subRuta.generarTrayectoria(grid, fechaHora);
            double distancia = trayectoria.size()-1;
            distanciaAcumulada += distancia;
            
            System.out.printf("%2d. %s -> %s (%.1f km | Total: %.1f km)\n",
                orden++,
                subRuta.getUbicacionInicio().detallarEnString(),
                subRuta.getUbicacionFin().detallarEnString(),
                distancia,
                distanciaAcumulada);
            System.out.print("Nodos: ");
            for(Nodo n: trayectoria){
                System.out.print(n.detallarEnString()+" ");
            }
            System.out.println("\n");
            System.out.println("Fecha de partida: "+ subRuta.getFechaPartida()+" Fecha de Llegada: "+subRuta.getFechaLlegada());
            System.out.println("\n");
        }
        
        System.out.println("Tiempo total estimado: " + 
            Duration.between(asignacion.getFechaPartida(), Utilidades.calcularHoraFin(asignacion)).toMinutes()+ " minutos");
    }
    
    public static void mostrarPedidosNoAsignados(List<Pedido> pedidos) {
        long noAsignados = pedidos.stream().filter(p -> !p.isEntregado()).count();
        if (noAsignados > 0) {
            System.out.println("\n=== PEDIDOS NO ASIGNADOS (" + noAsignados + ") ===");
            pedidos.stream()
                .filter(p -> !p.isEntregado())
                .forEach(p -> System.out.println(
                    "Pedido " + p.getId() + " para " + p.getIdCliente() + 
                    " (" + p.getCantidadGlp() + " m3) - Hora límite: " + 
                    p.getFechaMaximaEntrega().format(formatter)));
        }
    }
}