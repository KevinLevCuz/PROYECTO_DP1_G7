package Algoritmos.Genetico;

import java.awt.Point;
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

    double costoTotal() {
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
                consumoTotal += camion.calcularConsumoCombustible(distancia, pedido.getCantidad());
                origen = destino;
            }
            // Retorno a la planta (0,0) sin carga
            consumoTotal += camion.calcularConsumoCombustible(calcularDistanciaManhattan(origen, new Point(0, 0)), 0);
        }
        return consumoTotal;
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

}
