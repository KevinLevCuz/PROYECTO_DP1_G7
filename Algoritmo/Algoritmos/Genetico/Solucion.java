package Algoritmos.Genetico;

import java.awt.Point;
import java.time.LocalDateTime;
import java.util.*;

class Solucion {
    Map<Camion, List<Pedido>> rutas;

    Solucion() {
        this.rutas = new HashMap<>();
    }

    Solucion(Solucion otra) {
        // Copia profunda de las rutas (pedidos por camión)
        this.rutas = new HashMap<>();
        for (Map.Entry<Camion, List<Pedido>> entry : otra.rutas.entrySet()) {
            this.rutas.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
    }

    double costoTotal(List<Bloqueo> bloqueos,LocalDateTime fechaHoraActual) {
        double consumoTotal = 0.0;
        for (Map.Entry<Camion, List<Pedido>> entry : rutas.entrySet()) {
            Camion camion = entry.getKey();
            List<Pedido> pedidosAsignados = entry.getValue();
            
            // Verificar capacidad del camión
            int cargaActual = pedidosAsignados.stream().mapToInt(Pedido::getCantidad).sum();
            if (cargaActual > camion.getCapacidadEfectiva()) {
                return Double.MAX_VALUE; // Penalizar soluciones inválidas
            }
    
            Point origen = new Point(0, 0);
            for (Pedido pedido : pedidosAsignados) {
                Point destino = new Point(pedido.getUbicacionX(), pedido.getUbicacionY());
                double distancia = calcularDistanciaManhattan(origen, destino);

                // Verificar si hay bloqueos en este trayecto
                if (hayBloqueoEnTrayecto(origen, destino, bloqueos, fechaHoraActual)) {
                    return Double.MAX_VALUE; // Ruta inválida
                }

                consumoTotal += camion.calcularConsumoCombustible(distancia, pedido.getCantidad());
                origen = destino;
            }
            // Retorno a la planta (0,0) sin carga
            double distanciaRetorno = calcularDistanciaManhattan(origen, new Point(0, 0));
            if (hayBloqueoEnTrayecto(origen, new Point(0, 0), bloqueos, fechaHoraActual)) {
                return Double.MAX_VALUE;
            }
            consumoTotal += camion.calcularConsumoCombustible(distanciaRetorno, 0);
        }
        return consumoTotal;
    }
 
    private boolean hayBloqueoEnTrayecto(Point inicio, Point fin, List<Bloqueo> bloqueos, LocalDateTime fechaHora) {
        for (Bloqueo bloqueo : bloqueos) {
            // Verificar si el bloqueo está activo en este momento
            if (!bloqueo.estaActivo(fechaHora)) {
                continue;
            }
            
            // Verificar si algún nodo bloqueado está en este trayecto
            for (Point nodoBloqueado : bloqueo.getNodos()) {
                if (puntoEnLinea(inicio, fin, nodoBloqueado)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean puntoEnLinea(Point a, Point b, Point p) {
        // Verifica si el punto p está en la línea recta entre a y b (usando distancia Manhattan)
        return p.x >= Math.min(a.x, b.x) && p.x <= Math.max(a.x, b.x) &&
               p.y >= Math.min(a.y, b.y) && p.y <= Math.max(a.y, b.y);
    }

    private double calcularDistanciaManhattan(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    void imprimirRutas() {
        for (Map.Entry<Camion, List<Pedido>> entry : rutas.entrySet()) {
            Camion camion = entry.getKey();
            List<Pedido> pedidos = entry.getValue();
            System.out.println("Camión " + camion.getCodigo() + " (" + camion.getTipo() + "):");

            Point origen = new Point(0, 0);
            double consumoCamion = 0.0;

            for (Pedido p : pedidos) {
                Point destino = new Point(p.getUbicacionX(), p.getUbicacionY());
                double distancia = calcularDistanciaManhattan(origen, destino);
                double consumo = camion.calcularConsumoCombustible(distancia, p.getCantidad());
                consumoCamion += consumo;

                //System.out.println("  Planta (" + origen.x + "," + origen.y + ") → Cliente ("
                  //      + destino.x + "," + destino.y + ") [Carga: " + p.getCantidad() + "m³, Distancia: "
                    //    + distancia + ", Consumo: " + String.format("%.2f", consumo) + "]");

                origen = destino; // Para el siguiente pedido, el origen es el destino actual
            }

            // Regreso a planta (0,0) sin carga
            double distanciaRegreso = calcularDistanciaManhattan(origen, new Point(0, 0));
            double consumoRegreso = camion.calcularConsumoCombustible(distanciaRegreso, 0);
            consumoCamion += consumoRegreso;

            //System.out.println("  Retorno a Planta (0,0) [Sin carga, Distancia: "
               //     + distanciaRegreso + ", Consumo: " + String.format("%.2f", consumoRegreso) + "]");
            //System.out.println("  Consumo total de este camión: " + String.format("%.2f", consumoCamion));
            //System.out.println();
        }
    }
    public void imprimirRutasDetalladas(List<Camion> camiones, List<Bloqueo> bloqueos) {
        System.out.println("\n=== DETALLE DE RUTAS ===");
        
        for (Camion camion : rutas.keySet()) {
            if (!camion.getDisponible()) {
                System.out.println("\n🚛 Camión " + camion.getCodigo() + " - NO DISPONIBLE (en mantenimiento)");
                continue;
            }
            
            List<Pedido> ruta = rutas.get(camion);
            int cargaTotal = ruta.stream().mapToInt(Pedido::getCantidad).sum();
            
            if(ruta.size() != 0){
                System.out.printf("\n🚛 Camión %s (%s) - Capacidad: %d/%d m3 - Ruta: %d pedidos%n",
                    camion.getCodigo(),
                    camion.getTipo(),
                    cargaTotal,
                    camion.getCapacidadEfectiva(),
                    ruta.size());
            }
            
            
            // Verificar bloqueos para cada segmento de la ruta
            Point ubicacionActual = new Point(0, 0); // Asumiendo que el depósito está en (0,0)
            for (Pedido pedido : ruta) {
                Point destino = new Point(pedido.getUbicacionX(), pedido.getUbicacionY());
                System.out.printf("   📦 Pedido %d → Cliente %s (%d m3) - Entrega: (%d, %d)%n",
                        pedido.getId(),
                        pedido.getCliente().getId(),
                        pedido.getCantidad(),
                        destino.x, destino.y);
                
                
                ubicacionActual = destino;
            }
        }
    }

}
