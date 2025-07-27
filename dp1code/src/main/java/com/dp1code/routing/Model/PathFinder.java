package com.dp1code.routing.Model;

import java.time.LocalDateTime;
import java.util.*;

/**
 * PathFinder para cuadrícula con bloqueos dinámicos y mantenimientos de camión.
 * Implementa una búsqueda A* con heurística Manhattan.
 */
public class PathFinder {

    public static Map.Entry<ArrayList<Nodo>, LocalDateTime> generarTrayectoria(
            Grid grid, Nodo start, Nodo end,
            LocalDateTime fechaSimulada, LocalDateTime fechaMaximaLlegada,
            LocalDateTime fechaMinimaLlegada, LocalDateTime fechaMinimaSalida, LocalDateTime fechaMaximaSalida,
            Camion camion) {

        System.out.println("[DEBUG] generarTrayectoria inicio: start=" + start + ", end=" + end + ", fechaSimulada=" + fechaSimulada);
        LocalDateTime fechaActual = fechaSimulada.isBefore(fechaMinimaSalida)
                ? fechaMinimaSalida : fechaSimulada;
        
        if (fechaActual.isAfter(fechaMaximaSalida)) {
            System.out.println("[DEBUG] fechaActual (" + fechaActual + ") > fechaMaximaSalida (" + fechaMaximaSalida + "). No se puede iniciar la trayectoria.");
            return new AbstractMap.SimpleEntry<>(new ArrayList<>(), fechaActual);
        }
        System.out.println("[DEBUG] fechaActual ajustada: " + fechaActual);


        while (!fechaActual.isAfter(fechaMaximaLlegada) && !fechaActual.isAfter(fechaMaximaSalida)) {
            System.out.println("[DEBUG] while: fechaActual=" + fechaActual + ", fechaMaximaLlegada=" + fechaMaximaLlegada);
            if (!camion.isDisponiblePorMantenimiento(fechaActual)) {
                System.out.println("[DEBUG] Camion en mantenimiento en " + fechaActual);
                int segs = calcularTiempoFinMantenimiento(camion, fechaActual);
                System.out.println("[DEBUG] segundosHastaDisponible=" + segs);
                if (segs == -1 || fechaActual.plusSeconds(segs).isAfter(fechaMaximaLlegada)) {
                    System.out.println("[DEBUG] mantenimiento excede ventana, cancelando ruta");
                    return new AbstractMap.SimpleEntry<>(new ArrayList<>(), fechaActual);
                }
                fechaActual = fechaActual.plusSeconds(segs);
                if (fechaActual.isAfter(fechaMaximaSalida)) {
                    System.out.println("[DEBUG] Superada fecha máxima de salida, cancelando");
                    return new AbstractMap.SimpleEntry<>(new ArrayList<>(), fechaActual);
                }
                continue;
            }

            resetGrid(grid);
            System.out.println("[DEBUG] Grid reseteada para A*");

            PriorityQueue<Nodo> openList = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
            Set<Nodo> closedSet = new HashSet<>();

            start.g = 0;
            start.h = heuristic(start, end);
            start.f = start.h;
            openList.add(start);
            System.out.println("[DEBUG] nodo inicial agregado: " + start);

            ArrayList<Nodo> rutaFinal = null;
            LocalDateTime tiempoLlegada;

            while (!openList.isEmpty()) {
               // System.out.println("[DEBUG] openList size=" + openList.size());
                Nodo actual = openList.poll();
                //System.out.println("[DEBUG] expandiendo nodo: " + actual.getPosX() + "," + actual.getPosY());

                if (actual.equals(end)) {
                    System.out.println("[DEBUG] nodo final alcanzado: " + actual);
                    rutaFinal = backtrace(actual);
                    long duracionSegs = (rutaFinal.size() - 1) * 72;
                    tiempoLlegada = fechaActual.plusSeconds(duracionSegs);
                    System.out.println("[DEBUG] rutaFinal size=" + rutaFinal.size() + ", tiempoLlegada=" + tiempoLlegada);

                    if (tiempoLlegada.isBefore(fechaMinimaLlegada)) {
                        System.out.println("[DEBUG] llegada antes de minima, ajustando salida");
                        LocalDateTime salidaOptima = fechaMinimaLlegada.minusSeconds(duracionSegs);
                        if (salidaOptima.isAfter(fechaActual) && !salidaOptima.isAfter(fechaMaximaLlegada)) {
                            fechaActual = salidaOptima;
                            if (fechaActual.isAfter(fechaMaximaSalida)) {
                                System.out.println("[DEBUG] Superada fecha máxima de salida, cancelando");
                                return new AbstractMap.SimpleEntry<>(new ArrayList<>(), fechaActual);
                            }
                            System.out.println("[DEBUG] nueva fechaActual=" + fechaActual);
                            break;
                        }
                        long extra = java.time.Duration.between(tiempoLlegada, fechaMinimaLlegada).getSeconds() + duracionSegs;
                        if (fechaActual.plusSeconds(extra).isAfter(fechaMaximaLlegada)) {
                            System.out.println("[DEBUG] excede ventana tras ajuste, cancelando ruta");
                            return new AbstractMap.SimpleEntry<>(new ArrayList<>(), fechaActual);
                        }
                        fechaActual = fechaActual.plusSeconds(extra);
                        if (fechaActual.isAfter(fechaMaximaSalida)) {
                            System.out.println("[DEBUG] Superada fecha máxima de salida, cancelando");
                            return new AbstractMap.SimpleEntry<>(new ArrayList<>(), fechaActual);
                        }
                        System.out.println("[DEBUG] fechaActual pos ajuste=" + fechaActual);
                        break;
                    }

                    if (tiempoLlegada.isAfter(fechaMaximaLlegada)) {
                        System.out.println("[DEBUG] llegada despues de maxima, saliendo loop");
                        break;
                    }

                    System.out.println("[DEBUG] retornando rutaFinal");
                    return new AbstractMap.SimpleEntry<>(rutaFinal, fechaActual);
                }

                closedSet.add(actual);
                expandirVecinos(grid, actual, end, fechaActual, fechaMaximaLlegada, openList, closedSet);
            }

            if (rutaFinal == null) {
                int segs = calcularProximoCambio(grid, fechaActual);
                System.out.println("[DEBUG] no se encontro ruta, segundos hasta proximo cambio=" + segs);
                if (segs == 0 || fechaActual.plusSeconds(segs).isAfter(fechaMaximaLlegada)) {
                    System.out.println("[DEBUG] no hay proximo cambio viable, cancelando");
                    return new AbstractMap.SimpleEntry<>(new ArrayList<>(), fechaActual);
                }
                fechaActual = fechaActual.plusSeconds(segs);
                if (fechaActual.isAfter(fechaMaximaSalida)) {
                    System.out.println("[DEBUG] Superada fecha máxima de salida, cancelando");
                    return new AbstractMap.SimpleEntry<>(new ArrayList<>(), fechaActual);
                }
            }
        }

        System.out.println("[DEBUG] ventana agotada, retornando vacio");
        return new AbstractMap.SimpleEntry<>(new ArrayList<>(), fechaActual);
    }

    private static void resetGrid(Grid grid) {
        //System.out.println("[DEBUG] resetGrid llamada");
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                Nodo n = grid.getNodoAt(x, y);
                n.g = Double.POSITIVE_INFINITY;
                n.h = 0;
                n.f = 0;
                n.parent = null;
            }
        }
    }

    private static int calcularTiempoFinMantenimiento(Camion camion, LocalDateTime now) {
        if (camion.getMantenimientos() == null) return -1;
        int minSegs = Integer.MAX_VALUE;
        for (TimeRange t : camion.getMantenimientos()) {
            if (t.contains(now)) {
                long rest = java.time.Duration.between(now, t.getEnd()).getSeconds();
                if (rest <= 0) rest = 1;
                minSegs = (int)Math.min(minSegs, rest);
            }
        }
        //System.out.println("[DEBUG] calcularTiempoFinMantenimiento retornando=" + (minSegs == Integer.MAX_VALUE ? -1 : minSegs));
        return minSegs == Integer.MAX_VALUE ? -1 : minSegs;
    }

    private static int calcularProximoCambio(Grid grid, LocalDateTime now) {
        int minSegs = Integer.MAX_VALUE;
         
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                int seg = grid.getNodoAt(x, y).segundosParaProximoCambio(now);
                if (seg > 0 && seg < minSegs) minSegs = seg;
            }
        } 
        
        System.out.println("[DEBUG] calcularProximoCambio retornando=" + (minSegs == Integer.MAX_VALUE ? 0 : minSegs));
        return minSegs == Integer.MAX_VALUE ? 0 : minSegs;
    }

    private static double heuristic(Nodo a, Nodo b) {
        double h = Math.abs(a.getPosX() - b.getPosX()) + Math.abs(a.getPosY() - b.getPosY());
        //System.out.println("[DEBUG] heuristic(" + a + "," + b + ")=" + h);
        return h;
    }

    private static ArrayList<Nodo> backtrace(Nodo nodo) {
        //System.out.println("[DEBUG] backtrace desde nodo=" + nodo);
        ArrayList<Nodo> path = new ArrayList<>();
        while (nodo != null) {
            path.add(0, nodo);
            nodo = nodo.parent;
        }
        //System.out.println("[DEBUG] path construido: " + path);
        return path;
    }

    private static void expandirVecinos(
            Grid grid, Nodo actual, Nodo end,
            LocalDateTime now, LocalDateTime max,
            PriorityQueue<Nodo> openList, Set<Nodo> closed) {
        //System.out.println("[DEBUG] expandirVecinos en nodo=" + actual + ", vecinos count=" + grid.getNeighbors(actual).size());
        for (Nodo v : grid.getNeighbors(actual)) {
            int pasos = (int)actual.g + 1;
            LocalDateTime lleg = now.plusSeconds(pasos * 72);
            if (lleg.isAfter(max) || v.isBlockedAt(lleg) || closed.contains(v)) {
            //    System.out.println("[DEBUG] vecino " + v + " saltado, lleg=" + lleg);
                continue;
            }
            double gTent = actual.g + 1;
            if (gTent < v.g) {
                v.parent = actual;
                v.g = gTent;
                v.h = heuristic(v, end);
                v.f = v.g + v.h;
                openList.remove(v);
                openList.add(v);
          //      System.out.println("[DEBUG] vecino agregado: " + v + ", f=" + v.f);
            }
        }
    }
}