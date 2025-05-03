package Algoritmos.SA;

import java.awt.Point;
import java.time.LocalDateTime;
import java.util.*;
import Algoritmos.SA.Bloqueo;

class Solucion {
    static final double PENALIZACION_POR_BLOQUEO = 1000000.0;
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

    /**
     * Calcula el consumo total de combustible de la solución.
     */
    double costoTotal(Set<Bloqueo> bloqueos) {
        double consumoTotal = 0.0;
        for (Map.Entry<Camion, List<Pedido>> entry : rutas.entrySet()) {
            Camion camion = entry.getKey();
            List<Pedido> pedidosAsignados = entry.getValue();

            // Asumimos que el camión parte desde la planta (0,0)
            Point origen = new Point(0, 0);
            int tiempoAcumulado = 0;

            for (Pedido pedido : pedidosAsignados) {
                Point destino = new Point(pedido.getUbicacionX(), pedido.getUbicacionY());
                List<Point> camino = generarCaminoEntre(origen, destino);
                for (Point nodo : camino) {
                    LocalDateTime tiempoNodo = calcularFechaHoraDesdeTiempo(pedido.getDia(), tiempoAcumulado);
                    if (estaNodoBloqueado(nodo, tiempoNodo, bloqueos)) {
                        consumoTotal += PENALIZACION_POR_BLOQUEO;
                    }
                    tiempoAcumulado++; // Suponiendo que cada paso toma 1 minuto o unidad de tiempo
                }



                double distancia = calcularDistanciaManhattan(origen, destino);
                consumoTotal += camion.calcularConsumoCombustible(distancia, pedido.getCantidad());
                origen = destino;
            }
            // Retorno a la planta (0,0) sin carga
            List<Point> caminoRetorno = generarCaminoEntre(origen, new Point(0, 0));
            for (Point nodo : caminoRetorno) {
                LocalDateTime tiempoNodo = calcularFechaHoraDesdeTiempo(1, tiempoAcumulado); // Asumimos día 1 si no tienes fecha real
                if (estaNodoBloqueado(nodo, tiempoNodo, bloqueos)) {
                    consumoTotal += PENALIZACION_POR_BLOQUEO;
                }
                tiempoAcumulado++;
            }



            consumoTotal += camion.calcularConsumoCombustible(calcularDistanciaManhattan(origen, new Point(0, 0)), 0);
        }
        return consumoTotal;
    }

    /**
     * Calcula la distancia Manhattan (movimiento horizontal y vertical).
     */
    private double calcularDistanciaManhattan(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    /**
     * Imprime las rutas de cada camión.
     */
    /*
     * public void imprimirRutas() {
     * for (Map.Entry<Camion, List<Pedido>> entry : rutas.entrySet()) {
     * Camion camion = entry.getKey();
     * List<Pedido> pedidos = entry.getValue();
     * System.out.println(camion.getCodigo() + " -> Pedidos: ");
     * for (Pedido p : pedidos) {
     * System.out.println("  Pedido ID: " + p.getId() +
     * " Cliente: " + p.getCliente().getIdentificadorCliente() +
     * " Ubicación: (" + p.getUbicacionX() + ", " + p.getUbicacionY() + ")" +
     * " Cantidad: " + p.getCantidad());
     * }
     * }
     * }
     */
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

                System.out.println("  Planta (" + origen.x + "," + origen.y + ") → Cliente ("
                        + destino.x + "," + destino.y + ") [Carga: " + p.getCantidad() + "m³, Distancia: "
                        + distancia + ", Consumo: " + String.format("%.2f", consumo) + "]");

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

    private List<Point> generarCaminoEntre(Point origen, Point destino) {
        List<Point> camino = new ArrayList<>();
        int x = origen.x;
        int y = origen.y;

        while (x != destino.x) {
            x += (destino.x > x) ? 1 : -1;
            camino.add(new Point(x, y));
        }
        while (y != destino.y) {
            y += (destino.y > y) ? 1 : -1;
            camino.add(new Point(x, y));
        }
        return camino;
    }

    // ✅ Revisa si el nodo está bloqueado en la hora dada:
    private boolean estaNodoBloqueado(Point nodo, LocalDateTime tiempo, Set<Bloqueo> bloqueos) {
        for (Bloqueo bloqueo : bloqueos) {
            if (bloqueo.estaBloqueado(nodo, tiempo)) {
                return true;
            }
        }
        return false;
    }

    // ✅ Convierte tiempo acumulado en minutos a LocalDateTime (simplificado, ajusta si es necesario):
    private LocalDateTime calcularFechaHoraDesdeTiempo(int dia, int minutos) {
        int horas = (minutos / 60) % 24;
        int diasExtra = (minutos / 60) / 24;
        int minutosRestantes = minutos % 60;
        int diaFinal = dia + diasExtra;
        if (diaFinal > 31) {
            diaFinal = 31; // O podrías decidir que reinicie a 1 si quieres un calendario circular
        }
        return LocalDateTime.of(2025, 1, diaFinal, horas, minutosRestantes);
    }

}
