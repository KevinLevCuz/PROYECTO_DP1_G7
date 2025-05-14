package Algoritmos.SA2;

import java.time.LocalDateTime;
import java.util.*;

/**
 * PathFinder para cuadrícula con bloqueos dinámicos.
 * Usa A* con heurística Manhattan y bounding box para la ruta óptima
 * (mínimos pasos) entre dos nodos, evitando nodos bloqueados en t.
 */
public class PathFinder {

    /**
     * Calcula la ruta de menor número de pasos de 'start' a 'goal'.
     * Se evita explorar fuera de un bounding box definido por 'start' y 'goal'.
     *
     * @param start     Nodo origen
     * @param goal      Nodo destino
     * @param bloqueos  Lista de bloqueos dinámicos
     * @param t         Instante de simulación
     * @return ArrayList<Nodo> con la trayectoria completa de start a goal
     */
    public static ArrayList<Nodo> findPath(
            Nodo start,
            Nodo goal,
            List<Bloqueo> bloqueos,
            LocalDateTime t
    ) {
        // Definir bounding box alrededor de start y goal
        int dx = Math.abs(goal.getPosX() - start.getPosX());
        int dy = Math.abs(goal.getPosY() - start.getPosY());
        int margin = Math.max(dx, dy) + 10;
        int minX = Math.min(start.getPosX(), goal.getPosX()) - margin;
        int maxX = Math.max(start.getPosX(), goal.getPosX()) + margin;
        int minY = Math.min(start.getPosY(), goal.getPosY()) - margin;
        int maxY = Math.max(start.getPosY(), goal.getPosY()) + margin;

        Comparator<Node> cmp = Comparator.comparingInt(n -> n.f);
        PriorityQueue<Node> open = new PriorityQueue<>(cmp);
        Map<Nodo, Integer> gScore = new HashMap<>();
        Map<Nodo, Nodo> cameFrom = new HashMap<>();
        Set<Nodo> closed = new HashSet<>();

        gScore.put(start, 0);
        open.add(new Node(start, heuristic(start, goal)));

        while (!open.isEmpty()) {
            Node current = open.poll();
            Nodo u = current.n;
            if (closed.contains(u)) continue;
            closed.add(u);

            if (u.equals(goal)) {
                return reconstructPath(cameFrom, u);
            }

            // Expansión de vecinos N/S/E/O con bounding
            int[] dxs = {1, -1, 0, 0};
            int[] dys = {0, 0, 1, -1};
            for (int k = 0; k < 4; k++) {
                int nx = u.getPosX() + dxs[k];
                int ny = u.getPosY() + dys[k];
                if (nx < minX || nx > maxX || ny < minY || ny > maxY) continue;
                Nodo v = new Nodo(nx, ny);
                if (closed.contains(v) || isBlocked(v, bloqueos, t)) continue;
                int tentativeG = gScore.get(u) + 1;
                if (tentativeG < gScore.getOrDefault(v, Integer.MAX_VALUE)) {
                    cameFrom.put(v, u);
                    gScore.put(v, tentativeG);
                    int f = tentativeG + heuristic(v, goal);
                    open.add(new Node(v, f));
                }
            }
        }

        // No se encontró ruta dentro del bounding -> devolvemos solo el destino
        ArrayList<Nodo> single = new ArrayList<>();
        single.add(goal);
        return single;
    }

    private static ArrayList<Nodo> reconstructPath(Map<Nodo, Nodo> cameFrom, Nodo current) {
        ArrayList<Nodo> path = new ArrayList<>();
        while (current != null) {
            path.add(current);
            current = cameFrom.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    private static int heuristic(Nodo a, Nodo b) {
        return Math.abs(a.getPosX() - b.getPosX()) + Math.abs(a.getPosY() - b.getPosY());
    }

    private static boolean isBlocked(Nodo n, List<Bloqueo> bloqueos, LocalDateTime t) {
        for (Bloqueo b : bloqueos) {
            if (!t.isBefore(b.getInicio()) && !t.isAfter(b.getFin())
                    && b.getNodos().contains(n)) {
                return true;
            }
        }
        return false;
    }

    private static class Node {
        Nodo n;
        int f;
        Node(Nodo n, int f) { this.n = n; this.f = f; }
    }
}
