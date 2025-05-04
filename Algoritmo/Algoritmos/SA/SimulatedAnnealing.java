package Algoritmos.SA;

import java.util.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.Month;
import java.awt.Point;

public class SimulatedAnnealing {
    private List<Pedido> pedidos;
    private List<Camion> camiones;
    private Set<Bloqueo> bloqueos;

    private double T = 0.3;
    private final double Tmin = 0.05;
    private final double alpha = 0.85;
    private final int iterPerTemp = 3;

    public SimulatedAnnealing(List<Pedido> pedidos, List<Camion> camiones, Set<Bloqueo> bloqueos) {
        this.pedidos = pedidos;
        this.camiones = camiones;
        this.bloqueos = bloqueos;
    }
    
    public Solucion ejecutar() {
        Solucion current = generarSolucionInicial(pedidos, camiones, bloqueos/*, camionesEnMantenimiento */);
        System.out.println("El costo total es:" + current.costoTotal(bloqueos));

        current.imprimirRutas();

        Solucion best = new Solucion(current);

        Random rand = new Random();

        while (T > Tmin) {
            for (int i = 0; i < iterPerTemp; i++) {
                Solucion neighbor = generarVecino(current, camiones, bloqueos/* , camionesEnMantenimiento*/ );
                double delta = neighbor.costoTotal(bloqueos) - current.costoTotal(bloqueos);
                if (delta < 0 || Math.exp(-delta / T) > rand.nextDouble()) {
                    current = neighbor;
                }
                if (current.costoTotal(bloqueos) < best.costoTotal(bloqueos)) {
                    best = new Solucion(current);
                }
            }
            T *= alpha;
        }
        System.out.println("Mejor consumo encontrado: " + best.costoTotal(bloqueos));
        best.imprimirRutas();

        return current;
    }

    static Solucion generarSolucionInicial(List<Pedido> pedidos, List<Camion> camiones,
            Set<Bloqueo> bloqueos/* , Set<Integer> enMantenimiento */) {
        /* Implementar lógica básica para asignar pedidos aleatoriamente */
        Map<Camion, List<Pedido>> rutas = new HashMap<>();
        Random rand = new Random();

        for (Pedido pedido : pedidos) {
            boolean asignado = false;
            int intentos = 0;
            while (!asignado && intentos < 100) {
                Camion camion = camiones.get(rand.nextInt(camiones.size()));
                if (camion.estaDisponible() &&
                        camion.getCapacidadEfectiva() >= pedido.getCantidad()) {

                    rutas.computeIfAbsent(camion, k -> new ArrayList<>()).add(pedido);
                    asignado = true;
                }
                intentos++;
            }
        }
        Solucion solucion = new Solucion();
        solucion.rutas = rutas;
        return solucion;
    }

    static Solucion generarVecino(Solucion solucion, List<Camion> camiones,
            Set<Bloqueo> bloqueos/* , Set<Integer> enMantenimiento */) {
        Solucion vecino = new Solucion(solucion); // Copia profunda
        Random rand = new Random();

        List<Camion> camionesDisponibles = new ArrayList<>(vecino.rutas.keySet());
        if (camionesDisponibles.size() < 2)
            return vecino;

        Camion camionOrigen = camionesDisponibles.get(rand.nextInt(camionesDisponibles.size()));
        List<Pedido> pedidosOrigen = vecino.rutas.get(camionOrigen);
        if (pedidosOrigen.isEmpty())
            return vecino;

        Pedido pedidoMovido = pedidosOrigen.remove(rand.nextInt(pedidosOrigen.size()));
        Camion camionDestino = camionesDisponibles.get(rand.nextInt(camionesDisponibles.size()));

        if (camionDestino.getCapacidadEfectiva() >= pedidoMovido.getCantidad()) {
            vecino.rutas.computeIfAbsent(camionDestino, k -> new ArrayList<>()).add(pedidoMovido);
        } else {
            pedidosOrigen.add(pedidoMovido); // Si no cabe, lo devolvemos
        }
        return vecino;
    }

}
