package com.dp1code.routing.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class SimulatedAnnealing {
    private static final double EARLY_PENALTY = 10.0;
    public static final double SPEED_KMH = 50.0;
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
            Solucion current = simularPedidosEnTiempoReal(pedidos, camiones, now);

            return current;
        /*Solucion current = initialSolution(pedidos, camiones, now);
        Solucion best = current;
        double temp = initialTemp;

        for (int i = 0; i < maxIterations; i++) {
            Solucion neighbor = neighborSolution(current, now);
            /*
             * double costC = cost(current);
             * double costN = cost(neighbor);
             */
            /*double fitC = fitness(current);
            double fitN = fitness(neighbor);
            if (fitN > fitC || Math.exp((fitN - fitC) / temp) > random.nextDouble()) {
                current = neighbor;
            }

            if (fitness(current) > fitness(best)) {
                best = current;
            }
            temp *= (1 - coolingRate);
        }
        return best;*/
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

    /*
     * private LocalDateTime avanzarTiempo(LocalDateTime t, List<Nodo> traj) {
     * double dist = 0;
     * for (int i = 1; i < traj.size(); i++) {
     * dist += distance(traj.get(i - 1), traj.get(i));
     * }
     * double horas = dist / SPEED_KMH;
     * long H = (long) horas;
     * long M = (long) ((horas - H) * 60);
     * return t.plusHours(H).plusMinutes(M);
     * }
     */
    // Dentro de la clase SimulatedAnnealing
    private LocalDateTime avanzarTiempo(LocalDateTime t, List<Nodo> trayectoria) {
        double dist = 0;
        for (int i = 1; i < trayectoria.size(); i++) {
            Nodo a = trayectoria.get(i - 1);
            Nodo b = trayectoria.get(i);
            dist += distance(a, b);
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

    /*************************** NUEVAS FUNCIONES ***************************/
    /**
     * Recorre todas las subrutas de 'plan' (que ya contienen tiemposPorNodo)
     * y sitúa al camión en la posición apropiada para el relojObjetivo.
     */
    /**
     * Actualiza la posición y el GLP de un camión hasta el instante
     * 'relojObjetivo'.
     * Recorre cada SubRuta de su Plan, calcula los tiempos de llegada a cada Nodo y
     * resta el GLP consumido en cada tramo. Si el camión ya completó todas sus
     * SubRutas,
     * regresa a la base. Finalmente, asigna a c.ubicacionActual el Nodo
     * correspondiente
     * al instante 'relojObjetivo'.
     */
    public void actualizarCamionHasta(PlanCamion plan, LocalDateTime relojObjetivo) {
        Camion c = plan.getCamion();
        // 1) Determinar la base (planta principal)
        Nodo base = plantas.get(0).getUbicacion();

        // 2) Partimos del tanque actual de GLP del camión
        double glp = c.getGlpActual();

        // 3) Por defecto, la posición inicial es la base
        Nodo posicionActual = base;

        // 4) Si ya existen SubRutas, el "reloj" arranca en la hora de inicio de la
        // primera SubRuta;
        // si no, arrancamos directamente en el instante que nos piden (queda en base).
        LocalDateTime t = plan.getSubRutas().isEmpty()
                ? relojObjetivo
                : plan.getSubRutas().get(0).getHoraInicio();

        // 5) Recorremos cada SubRuta asignada al camión en orden
        for (SubRuta sr : plan.getSubRutas()) {
            List<Nodo> trayectoria = sr.getTrayectoria();
            LocalDateTime inicioSR = sr.getHoraInicio();

            // 5.A) Si aún no hemos llegado al inicio de esta SubRuta, el camión permanece
            // en la posiciónActual
            if (relojObjetivo.isBefore(inicioSR)) {
                c.setUbicacionActual(posicionActual);
                c.setGlpActual(glp);
                return;
            }

            // 5.B) Si ya superamos la hora de inicio, avanzamos tramo a tramo
            LocalDateTime tiempoNodo = inicioSR;
            // La primera posición de la trayectoria suele coincidir con el punto de partida
            // de la SubRuta
            posicionActual = trayectoria.get(0);

            for (int i = 1; i < trayectoria.size(); i++) {
                Nodo prev = trayectoria.get(i - 1);
                Nodo next = trayectoria.get(i);

                // 5.B.i) Calculamos la distancia Euclidiana entre prev y next
                double dist = distance(prev, next);

                // 5.B.ii) Transformamos esa distancia en horas de viaje
                double horas = dist / SPEED_KMH;
                long H = (long) horas;
                long M = (long) ((horas - H) * 60);

                // 5.B.iii) Avanzamos el reloj hasta la hora de llegada a 'next'
                tiempoNodo = tiempoNodo.plusHours(H).plusMinutes(M);

                // 5.B.iv) Calculamos el GLP consumido en este tramo
                double consumo = dist * (c.getCapacidadMaxima() / 180.0);

                // 5.B.v) Si 'tiempoNodo' ya es posterior o igual a 'relojObjetivo', estamos
                // entre prev y next,
                // por lo que el camión se queda en 'prev'
                if (tiempoNodo.isAfter(relojObjetivo)) {
                    posicionActual = prev;
                    c.setUbicacionActual(posicionActual);
                    c.setGlpActual(glp);
                    return;
                }

                // 5.B.vi) Si aún no llegó al instante deseado, completa ese tramo:
                posicionActual = next;
                glp -= consumo;
            }

            // 5.C) Si completó toda la SubRuta antes del 'relojObjetivo', entonces queda al
            // final de la SubRuta
            posicionActual = sr.getFin();
            // (el GLP ya se fue descontando tramo a tramo dentro del for anterior)
        }

        // 6) Si ya terminó todas sus SubRutas y aún no llegó el instante
        // 'relojObjetivo',
        // debe regresar a la base:
        if (!posicionActual.equals(base)) {
            List<Nodo> trajBack = PathFinder.findPath(posicionActual, base, bloqueos, t);
            LocalDateTime tiempoNodo = t;

            for (int i = 1; i < trajBack.size(); i++) {
                Nodo prev = trajBack.get(i - 1);
                Nodo next = trajBack.get(i);

                double dist = distance(prev, next);
                double horas = dist / SPEED_KMH;
                long H = (long) horas;
                long M = (long) ((horas - H) * 60);

                tiempoNodo = tiempoNodo.plusHours(H).plusMinutes(M);
                double consumo = dist * (c.getCapacidadMaxima() / 180.0);

                if (tiempoNodo.isAfter(relojObjetivo)) {
                    posicionActual = prev;
                    c.setUbicacionActual(posicionActual);
                    c.setGlpActual(glp);
                    return;
                }

                // Si aún no llegamos al instante, completamos ese tramo de regreso a base
                posicionActual = next;
                glp -= consumo;
            }

            // Cuando finalmente completa la vuelta a la base:
            posicionActual = base;
        }

        // 7) Asignamos al camión su última posición y su GLP remanente (si es negativo,
        // lo fijamos en 0)
        c.setUbicacionActual(posicionActual);
        c.setGlpActual(glp < 0 ? 0 : glp);
    }

    /**
     * Reemplaza la antigua initialSolution(...).
     * - Recorre los pedidos en orden de llegada
     * - Actualiza todos los camiones hasta la hora de cada pedido
     * - Asigna el pedido al camión más conveniente
     * - Al final hace que cada camión regrese a la base si queda lejos
     */
    public Solucion simularPedidosEnTiempoReal(
            ArrayList<Pedido> pedidos,
            ArrayList<Camion> camiones,
            LocalDateTime ahora) {

        Nodo base = plantas.get(0).getUbicacion();
        // 1) Inicializar cada camión en base con tanque lleno
        ArrayList<PlanCamion> planes = new ArrayList<>();
        for (Camion c : camiones) {
            c.setUbicacionActual(base);
            c.setGlpActual(c.getCapacidadMaxima());
            planes.add(new PlanCamion(c, new ArrayList<>()));
        }

        // 2) Ordenar pedidos por horaPedido ascendente
        pedidos.sort(Comparator.comparing(Pedido::getHoraPedido));

        LocalDateTime tiempoActual = ahora;

        // 3) Para cada pedido en orden de llegada:
        for (Pedido p : pedidos) {
            tiempoActual = p.getHoraPedido();

            LocalDateTime reloj = tiempoActual;

            // 3.A) Antes de asignar este pedido, “adelantamos” cada camión a 'reloj'
            for (PlanCamion plan : planes) {
                actualizarCamionHasta(plan, reloj);
            }

            // 3.B) Seleccionar el camión disponible más cercano (o lógica que ya usabas)
            PlanCamion mejorPlan = null;
            double mejorDist = Double.MAX_VALUE;
            for (PlanCamion plan : planes) {
                Camion c = plan.getCamion();
                // Solo consideramos camiones que, al terminar su última subruta, queden libres
                // (es decir, cuyo relojLocal ≤ reloj) y no estén en mantenimiento en 'reloj'
                boolean enMant = mantenimientos.stream().anyMatch(m -> m.getCodigoCamion().equals(c.getCodigo()) &&
                        !reloj.isBefore(m.getInicio()) &&
                        reloj.isBefore(m.getFin()));
                if (enMant)
                    continue;

                LocalDateTime tCam = plan.getSubRutas().isEmpty()
                        ? reloj
                        : plan.getSubRutas().get(plan.getSubRutas().size() - 1).getHoraFin();
                if (tCam.isAfter(reloj))
                    continue; // aún en ruta, no disponible

                // Ubicación actual ya fue actualizada por actualizarCamionHasta
                Nodo ubic = c.getUbicacionActual();
                double dist = distance(ubic, p.getDestino());
                if (dist < mejorDist) {
                    mejorDist = dist;
                    mejorPlan = plan;
                }
            }

            // 3.C) Si no hay camión disponible, saltamos el pedido
            if (mejorPlan == null)
                continue;

            // 3.D) Generar subrutas para llevar del camión → pedido:
            Camion elegido = mejorPlan.getCamion();
            Nodo inicio = elegido.getUbicacionActual();
            LocalDateTime tInicio = reloj;

            // 3.D.a) Espera mínima de 4 h:
            LocalDateTime earliest = p.getHoraPedido().plusHours(4);
            if (tInicio.isBefore(earliest))
                tInicio = earliest;

            // 3.D.b) Verificar si necesita recarga antes de ir al pedido
            double distHastaPedido = distance(inicio, p.getDestino());
            double neededGLP = distHastaPedido * (elegido.getCapacidadMaxima() / 180.0);
            if (elegido.getGlpActual() < neededGLP) {
                // elegimos planta más cercana para recargar
                Planta mejor = null;
                double bestDist2 = Double.MAX_VALUE;
                for (Planta pl : plantas) {
                    double d2 = distance(inicio, pl.getUbicacion());
                    if (d2 < bestDist2) {
                        bestDist2 = d2;
                        mejor = pl;
                    }
                }
                // Crear subruta recarga
                ArrayList<Nodo> trajRec = PathFinder.findPath(inicio, mejor.getUbicacion(), bloqueos, tInicio);
                // Calcular tiemposPorNodo
                ArrayList<LocalDateTime> tiemposRec = new ArrayList<>();
                LocalDateTime t0 = tInicio;
                tiemposRec.add(t0);
                for (int i = 1; i < trajRec.size(); i++) {
                    double d = distance(trajRec.get(i - 1), trajRec.get(i));
                    double horas = d / SPEED_KMH;
                    long H = (long) horas;
                    long M = (long) ((horas - H) * 60);
                    t0 = t0.plusHours(H).plusMinutes(M);
                    tiemposRec.add(t0);
                }
                LocalDateTime tFinRec = tiemposRec.get(tiemposRec.size() - 1);
                mejorPlan.addSubRuta(new SubRuta(
                        inicio,
                        mejor.getUbicacion(),
                        null, // null = solo recarga
                        trajRec,
                        tiemposRec,
                        tInicio,
                        tFinRec));
                elegido.setGlpActual(elegido.getCapacidadMaxima());
                inicio = mejor.getUbicacion();
                tInicio = tFinRec;
            }

            // 3.D.c) Ahora sí, ruta hasta el nodo del pedido
            ArrayList<Nodo> trajEnt = PathFinder.findPath(inicio, p.getDestino(), bloqueos, tInicio);
            ArrayList<LocalDateTime> tiemposEnt = new ArrayList<>();
            LocalDateTime t1 = tInicio;
            tiemposEnt.add(t1);
            for (int i = 1; i < trajEnt.size(); i++) {
                double d = distance(trajEnt.get(i - 1), trajEnt.get(i));
                double horas = d / SPEED_KMH;
                long H = (long) horas;
                long M = (long) ((horas - H) * 60);
                t1 = t1.plusHours(H).plusMinutes(M);
                tiemposEnt.add(t1);
            }
            LocalDateTime tFinEnt = tiemposEnt.get(tiemposEnt.size() - 1);

            // 3.D.d) Si supera el plazo máximo, descartamos la subruta (no lo añadimos)
            if (tFinEnt.isAfter(p.getPlazoMaximoEntrega())) {
                continue;
            }

            // 3.D.e) Añadimos la subruta de entrega
            mejorPlan.addSubRuta(new SubRuta(
                    inicio,
                    p.getDestino(),
                    p,
                    trajEnt,
                    tiemposEnt,
                    tInicio,
                    tFinEnt));
            // descontar GLP del trayecto al pedido
            elegido.setGlpActual(elegido.getGlpActual() - neededGLP);
            elegido.setUbicacionActual(p.getDestino());
        }

        // 4) Una vez asignados todos los pedidos, cada camión regresa a base si está
        // lejos
        for (PlanCamion plan : planes) {
            Camion c = plan.getCamion();
            Nodo pos = c.getUbicacionActual();
            LocalDateTime tUlt = plan.getSubRutas().isEmpty()
                    ? tiempoActual
                    : plan.getSubRutas().get(plan.getSubRutas().size() - 1).getHoraFin();
            if (!pos.equals(base)) {
                ArrayList<Nodo> trajBack = PathFinder.findPath(pos, base, bloqueos, tUlt);
                ArrayList<LocalDateTime> tiemposBack = new ArrayList<>();
                LocalDateTime t0 = tUlt;
                tiemposBack.add(t0);
                for (int i = 1; i < trajBack.size(); i++) {
                    double d = distance(trajBack.get(i - 1), trajBack.get(i));
                    double horas = d / SPEED_KMH;
                    long H = (long) horas;
                    long M = (long) ((horas - H) * 60);
                    t0 = t0.plusHours(H).plusMinutes(M);
                    tiemposBack.add(t0);
                }
                LocalDateTime tFinBack = tiemposBack.get(tiemposBack.size() - 1);
                plan.addSubRuta(new SubRuta(
                        pos,
                        base,
                        null,
                        trajBack,
                        tiemposBack,
                        tUlt,
                        tFinBack));
                // descontar GLP del regreso
                double totalDist = 0;
                for (int i = 1; i < trajBack.size(); i++) {
                    totalDist += distance(trajBack.get(i - 1), trajBack.get(i));
                }
                c.setGlpActual(Math.max(0, c.getGlpActual() - totalDist * (c.getCapacidadMaxima() / 180.0)));
                c.setUbicacionActual(base);
            }
        }

        Solucion sol = new Solucion(planes, 0);
        sol.setCosto(cost(sol));
        return sol;
    }

}
