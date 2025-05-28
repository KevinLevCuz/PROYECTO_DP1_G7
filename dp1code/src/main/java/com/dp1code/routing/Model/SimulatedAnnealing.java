package com.dp1code.routing.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class SimulatedAnnealing {
    private static final double EARLY_PENALTY = 10.0;
    private static final double SPEED_KMH = 50.0;
    private static final double MAINT_PENALTY = 10_000.0;
    private static final double BLOCK_PENALTY = 10_000.0;
    private static final double DEADLINE_PENALTY = 10_000.0;
    private double initialTemp;
    private double coolingRate;
    private int maxIterations;
    private Random random = new Random();
    private ArrayList<Planta> plantas;
    private ArrayList<Bloqueo> bloqueos;
    private ArrayList<Mantenimiento> mantenimientos;

    /**
     * @param initialTemp   Temperatura inicial
     * @param coolingRate   Tasa de enfriamiento (ej: 0.003)
     * @param maxIterations Número máximo de iteraciones
     */
    public SimulatedAnnealing(double initialTemp,
            double coolingRate,
            int maxIterations,
            ArrayList<Planta> plantas,
            ArrayList<Bloqueo> bloqueos,
            ArrayList<Mantenimiento> mantenimientos) {
        this.initialTemp = initialTemp;
        this.coolingRate = coolingRate;
        this.maxIterations = maxIterations;
        this.plantas = plantas;
        this.bloqueos = bloqueos;
        this.mantenimientos = mantenimientos;
    }

    /**
     * Ejecuta SA y retorna la mejor solución encontrada.
     */
    public Solucion optimize(
            ArrayList<Pedido> pedidos,
            ArrayList<Camion> camiones,
            ArrayList<Planta> plantas,
            ArrayList<Bloqueo> bloqueos,
            ArrayList<Mantenimiento> mantenimientos,
            LocalDateTime now) {
        Solucion current = initialSolution(pedidos, camiones, now);
        Solucion best = current;
        double temp = initialTemp;

        for (int i = 0; i < maxIterations; i++) {
            Solucion neighbor = neighborSolution(current, now);
            /*double costC = cost(current);
            double costN = cost(neighbor);*/
            double fitC = fitness(current);
            double fitN = fitness(neighbor);
            if (fitN > fitC || Math.exp((fitN - fitC) / temp) > random.nextDouble()) {
                current = neighbor;
            }

            if (fitness(current) > fitness(best)) {
                best = current;
            }
            temp *= (1 - coolingRate);
        }
        return best;
    }

    /**
     * Construye una solución inicial: una ruta por camión, asignando pedidos
     * secuencialmente.
     */
    private Solucion initialSolution(
            ArrayList<Pedido> pedidos,
            ArrayList<Camion> camiones,
            LocalDateTime now) {
        Nodo base = plantas.get(0).getUbicacion(); // planta principal

        ArrayList<PlanCamion> plans = new ArrayList<>();
        for (Camion c : camiones) {
            c.setUbicacionActual(base);
            c.setGlpActual(c.getCapacidadMaxima());
            plans.add(new PlanCamion(c, new ArrayList<>()));
        }

        int idx = 0;
        for (Pedido p : pedidos) {
            PlanCamion plan = plans.get(idx % plans.size());
            Camion c = plan.getCamion();
            LocalDateTime t = now;
            Nodo start = c.getUbicacionActual();

            if (!plan.getSubRutas().isEmpty()) {
                SubRuta last = plan.getSubRutas().get(plan.getSubRutas().size() - 1);
                start = last.getFin();
                t = last.getHoraFin();
            }

            // Espera mínima de 4 h tras horaPedido
            LocalDateTime earliest = p.getHoraPedido().plusHours(4);
            if (t.isBefore(earliest)) {
                t = earliest;
            }

            // 1.A) Si no hay GLP suficiente, inserta recarga
            double distPedido = distance(start, p.getDestino());
            double neededGLP = distPedido * (c.getCapacidadMaxima() / 180.0);
            if (c.getGlpActual() < neededGLP) {
                // elige planta más cercana sin lambdas
                Planta mejor = null;
                double bestDist = Double.MAX_VALUE;
                for (Planta pl : plantas) {
                    double d = distance(start, pl.getUbicacion());
                    if (d < bestDist) {
                        bestDist = d;
                        mejor = pl;
                    }
                }
                ArrayList<Nodo> trajRec = PathFinder.findPath(start, mejor.getUbicacion(), bloqueos, t);
                LocalDateTime tRec = avanzarTiempo(t, trajRec);
                plan.addSubRuta(new SubRuta(start, mejor.getUbicacion(), null, trajRec, t, tRec));
                c.setGlpActual(c.getCapacidadMaxima());
                start = mejor.getUbicacion();
                t = tRec;
            }

            // 1.B) subruta al pedido
            ArrayList<Nodo> trajEnt = PathFinder.findPath(start, p.getDestino(), bloqueos, t);
            LocalDateTime tEnt = avanzarTiempo(t, trajEnt);

            // Saltar si supera el plazo máximo
            if (tEnt.isAfter(p.getPlazoMaximoEntrega())) {
                // no asignamos esta subruta en la solución inicial
                idx++;
                continue;
            }

            plan.addSubRuta(new SubRuta(start, p.getDestino(), p, trajEnt, t, tEnt));
            c.setGlpActual(c.getGlpActual() - neededGLP);
            c.setUbicacionActual(p.getDestino());
            idx++;
        }

        // 1.C) retorno a base
        for (PlanCamion plan : plans) {
            if (!plan.getSubRutas().isEmpty()) {
                SubRuta last = plan.getSubRutas().get(plan.getSubRutas().size() - 1);
                Nodo s = last.getFin();
                LocalDateTime t = last.getHoraFin();
                if (!s.equals(base)) {
                    ArrayList<Nodo> trajB = PathFinder.findPath(s, base, bloqueos, t);
                    LocalDateTime tB = avanzarTiempo(t, trajB);
                    plan.addSubRuta(new SubRuta(s, base, null, trajB, t, tB));
                }
            }
        }

        Solucion sol = new Solucion(plans, 0);
        sol.setCosto(cost(sol));
        return sol;
    }

    /**
     * Genera un vecino intercambiando dos pedidos entre rutas
     */
    private Solucion neighborSolution(Solucion sol, LocalDateTime now) {
        ArrayList<PlanCamion> plans = new ArrayList<>();
        for (PlanCamion pc : sol.getPlanesCamion()) {
            PlanCamion copy = new PlanCamion(pc.getCamion(), new ArrayList<>());
            for (SubRuta sr : pc.getSubRutas()) {
                copy.addSubRuta(new SubRuta(
                        sr.getInicio(), sr.getFin(), sr.getPedido(),
                        new ArrayList<>(sr.getTrayectoria()),
                        sr.getHoraInicio(), sr.getHoraFin()));
            }
            plans.add(copy);
        }
        if (plans.size() >= 2) {
            int i = random.nextInt(plans.size());
            int j = random.nextInt(plans.size());
            PlanCamion p1 = plans.get(i), p2 = plans.get(j);
            if (!p1.getSubRutas().isEmpty() && !p2.getSubRutas().isEmpty()) {
                int r1 = random.nextInt(p1.getSubRutas().size());
                int r2 = random.nextInt(p2.getSubRutas().size());
                SubRuta tmp = p1.getSubRutas().get(r1);
                p1.getSubRutas().set(r1, p2.getSubRutas().get(r2));
                p2.getSubRutas().set(r2, tmp);
                recalcPlan(p1, now);
                recalcPlan(p2, now);
            }
        }
        Solucion neighbor = new Solucion(plans, 0);
        neighbor.setCosto(cost(neighbor));
        return neighbor;
    }

    private void recalcPlan(PlanCamion plan, LocalDateTime now) {
        Nodo base = plantas.get(0).getUbicacion();
        Camion c = plan.getCamion();
        LocalDateTime t = now;
        Nodo prev = c.getUbicacionActual();
        ArrayList<SubRuta> newSubs = new ArrayList<>();

        for (SubRuta sr : plan.getSubRutas()) {
            Pedido p = sr.getPedido();
            Nodo dest = sr.getFin();

            // Espera mínima si es entrega real
            if (p != null) {
                LocalDateTime earliest = p.getHoraPedido().plusHours(4);
                if (t.isBefore(earliest))
                    t = earliest;
            }

            // Recarga si falta GLP
            double distToDest = distance(prev, dest);
            double neededGLP = distToDest * (c.getCapacidadMaxima() / 180.0);
            if (c.getGlpActual() < neededGLP) {
                Planta mejor = null;
                double bestDist = Double.MAX_VALUE;
                for (Planta pl : plantas) {
                    double d = distance(prev, pl.getUbicacion());
                    if (d < bestDist) {
                        bestDist = d;
                        mejor = pl;
                    }
                }
                ArrayList<Nodo> trajRec = PathFinder.findPath(prev, mejor.getUbicacion(), bloqueos, t);
                LocalDateTime tRec = avanzarTiempo(t, trajRec);
                newSubs.add(new SubRuta(prev, mejor.getUbicacion(), null, trajRec, t, tRec));
                c.setGlpActual(c.getCapacidadMaxima());
                prev = mejor.getUbicacion();
                t = tRec;
            }

            // Subruta al destino (salta si incumple deadline)
            ArrayList<Nodo> trajEnt = PathFinder.findPath(prev, dest, bloqueos, t);
            LocalDateTime tEnt = avanzarTiempo(t, trajEnt);
            if (p == null || !tEnt.isAfter(p.getPlazoMaximoEntrega())) {
                newSubs.add(new SubRuta(prev, dest, p, trajEnt, t, tEnt));
                c.setGlpActual(c.getGlpActual() - neededGLP);
                prev = dest;
                t = tEnt;
            }
        }

        // Retorno a base
        if (!prev.equals(base)) {
            ArrayList<Nodo> trajBack = PathFinder.findPath(prev, base, bloqueos, t);
            LocalDateTime tBack = avanzarTiempo(t, trajBack);
            newSubs.add(new SubRuta(prev, base, null, trajBack, t, tBack));
        }

        plan.setSubRutas(newSubs);
    }

    /**
     * Calcula el costo total de la solución: distancia + penalización por retrasos
     * y consumo
     */
    public double cost(Solucion sol) {
        double totalCost = 0.0;
        for (PlanCamion plan : sol.getPlanesCamion()) {
            Camion c = plan.getCamion();
            for (SubRuta sr : plan.getSubRutas()) {
                LocalDateTime t = sr.getHoraInicio();
                Pedido p = sr.getPedido();

                // Early-penalty solo si es un pedido real
                if (p != null) {
                    LocalDateTime earliest = p.getHoraPedido().plusHours(4);
                    if (t.isBefore(earliest))
                        totalCost += EARLY_PENALTY;
                }

                // Mantenimiento (vale para recarga o entrega)
                for (Mantenimiento m : mantenimientos) {
                    if (m.getCodigoCamion().equals(c.getCodigo())
                            && !t.isBefore(m.getInicio())
                            && t.isBefore(m.getFin())) {
                        totalCost += MAINT_PENALTY;
                    }
                }

                // Distancia, consumo y bloqueos
                double glp = c.getGlpActual();
                LocalDateTime tu = t;
                for (int k = 1; k < sr.getTrayectoria().size(); k++) {
                    Nodo a = sr.getTrayectoria().get(k - 1);
                    Nodo b = sr.getTrayectoria().get(k);
                    double d = distance(a, b);
                    double consumo = d * (c.getCapacidadMaxima() / 180.0);
                    totalCost += d + consumo;

                    double horas = d / SPEED_KMH;
                    long H = (long) horas;
                    long M = (long) ((horas - H) * 60);
                    tu = tu.plusHours(H).plusMinutes(M);
                    glp -= consumo;

                    for (Bloqueo bl : bloqueos) {
                        if (!tu.isBefore(bl.getInicio())
                                && !tu.isAfter(bl.getFin())
                                && bl.getNodos().contains(b)) {
                            totalCost += BLOCK_PENALTY;
                        }
                    }
                }

                // Deadline-penalty solo si es un pedido real
                if (p != null && tu.isAfter(p.getPlazoMaximoEntrega())) {
                    totalCost += DEADLINE_PENALTY;
                }
            }
        }
        return totalCost;
    }

    private LocalDateTime avanzarTiempo(LocalDateTime t, List<Nodo> traj) {
        double dist = 0;
        for (int i = 1; i < traj.size(); i++) {
            dist += distance(traj.get(i - 1), traj.get(i));
        }
        double horas = dist / SPEED_KMH;
        long H = (long) horas;
        long M = (long) ((horas - H) * 60);
        return t.plusHours(H).plusMinutes(M);
    }

    private boolean accept(double costC, double costN, double temp) {
        if (costN < costC)
            return true;
        return Math.exp(-(costN - costC) / temp) > random.nextDouble();
    }

    private double distance(Nodo a, Nodo b) {
        double dx = a.getPosX() - b.getPosX();
        double dy = a.getPosY() - b.getPosY();
        return Math.hypot(dx, dy);
    }

    private double fitness(Solucion sol) {
        double c = cost(sol);
        return 1.0 / (1.0 + c);
    }
}
