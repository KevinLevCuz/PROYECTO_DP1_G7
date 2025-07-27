package com.dp1code.routing.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import org.springframework.cglib.core.Local;

public class SimulatedAnnealing {
    private static final double EARLY_PENALTY = 10.0;
    public static final double SPEED_KMH = 50.0;
    public static final int horasPlazo = 4;
    private double initialTemp;
    private double coolingRate;
    private int maxIterations;
    private Random random = new Random();

    private ArrayList<Planta> plantas;
    private ArrayList<Camion> camiones;
    private ArrayList<Pedido> pedidos;
    private Grid grid;

    /**
     * @param initialTemp   Temperatura inicial
     * @param coolingRate   Tasa de enfriamiento (ej: 0.003)
     * @param maxIterations Número máximo de iteraciones
     */
    public SimulatedAnnealing(double initialTemp, double coolingRate, int maxIterations,
            ArrayList<Planta> plantas, ArrayList<Camion> camiones, ArrayList<Pedido> pedidos, Grid grid) {

        this.initialTemp = initialTemp;
        this.coolingRate = coolingRate;
        this.maxIterations = maxIterations;

        this.plantas = plantas;
        this.camiones = camiones;
        this.pedidos = pedidos;
        this.grid = grid;
    }

    /**
     * Ejecuta SA y retorna la mejor solución encontrada.
     */
    public Solucion optimize(LocalDateTime now) {

        Solucion current = initialSolution(now);
        //return current;
        
         Set<String> assignedIds = new HashSet<>();
         for (PlanCamion plan : current.getPlanesCamion()) {
         for (SubRuta sr : plan.getSubRutas()) {
         Pedido ped = sr.getPedido();
         if (ped != null) {
         assignedIds.add(ped.getId());
         }
         }
         }
         
         // 2) Recorremos la lista “pedidos” original y recogemos los que no estén en
//         assignedIds:
         List<Pedido> unassigned = new ArrayList<>();
         for (Pedido p : pedidos) {
         if (!assignedIds.contains(p.getId())) {
         unassigned.add(p);
         }
         }
         
         Solucion best = current;
         double temp = initialTemp;
         
         for (int i = 0; i < maxIterations; i++) {
         Solucion neighbor = neighborSolution(current, unassigned, now);
         
         double fitC = fitness(current);
         double fitN = fitness(neighbor);
         if (fitN > fitC || Math.exp((fitN - fitC) / temp) > random.nextDouble()) {
         System.out.println("INGRESOO A OTRO NEIGBOR");
         current = neighbor;
         }
         
         if (fitness(current) > fitness(best)) {
         System.out.println("CAMBIO A OTRA SOLUCIONNN");
         best = current;
         }
         temp *= (1 - coolingRate);
         }
         return best;
         
    }

    private Solucion initialSolution(LocalDateTime now) {
        // System.out.println("Ingreso aqui A INITIAL SOLUTION");
        Nodo base = plantas.get(0).getUbicacion(); // planta principal

        ArrayList<PlanCamion> plans = new ArrayList<>();
        Collections.reverse(camiones);

        for (Camion c : camiones) {
            plans.add(new PlanCamion(c, new ArrayList<>()));
            c.setGlpActualSim(c.getGlpActual());
            c.setGlpTanqueSim(c.getGlpTanque());
        }

        List<Pedido> todosPedidos = new ArrayList<>();
        for (Pedido p : pedidos) {
            if (p.getCantidadGlp() > 25.0) {
                // System.out.println("El pedido " + p.getId() + " es grande. Se va a
                // dividir.");
                todosPedidos.addAll(dividirPedidoGrande(p));
            } else {
                todosPedidos.add(p);
            }
        }
        todosPedidos.sort(Comparator.comparing(Pedido::getPlazoMaximoEntrega));

        List<Pedido> noAsignados = new ArrayList<>();

        for (Pedido p : todosPedidos) {
            System.out.println("\n== Intentando asignar pedido " + p.getId()
                    + " (GLP=" + p.getCantidadGlp()
                    + ", destino=" + p.getDestino().getPosX() + ", " + p.getDestino().getPosY()
                    + ", plazo=" + p.getPlazoMaximoEntrega() + ")");
            boolean asignado = false;
            for (PlanCamion plan : plans) {
                Camion c = plan.getCamion();
                System.out.println("  -> Camión " + c.getCodigo()
                        + " (capMax=" + c.getCapacidadMaxima()
                        + ", glpAct=" + c.getGlpActualSim()
                        + ", glpTan=" + c.getGlpTanqueSim() + ") y su ubicacion: " + c.getUbicacionActual().getPosX()
                        + ", " + c.getUbicacionActual().getPosY());
                if (p.getCantidadGlp() > c.getCapacidadMaxima()) {
                    System.out.println("     SKIP: pedido demasiado grande para camión");
                    continue;
                }
                for (Planta planta : plantas) {
                    planta.setGlpDisponibleSim(planta.getGlpDisponible());
                }
                LocalDateTime t = now;

                Nodo start = c.getUbicacionActual();
                if (!plan.getSubRutas().isEmpty()) {
                    SubRuta last = plan.getSubRutas().get(plan.getSubRutas().size() - 1);
                    start = last.getFin();
                    t = last.getHoraFin();
                    if (last.getPedido() != null) {
                        t = t.plusMinutes(15);
                    }
                }

                System.out.println("     Chequeando ruta directa de " + start.getPosX() + ", " + start.getPosY() + " a "
                        + p.getDestino().getPosX() + ", " + p.getDestino().getPosY()
                        + " a partir de " + t);
                Map.Entry<ArrayList<Nodo>, LocalDateTime> resultado = PathFinder.generarTrayectoria(
                        grid, start, p.getDestino(), t, p.getPlazoMaximoEntrega(),
                        p.getHoraPedido().plusHours(horasPlazo), t, p.getPlazoMaximoEntrega(), c);
                ArrayList<Nodo> trayectoria = resultado.getKey();
                LocalDateTime horaSalida = resultado.getValue();
                LocalDateTime horaLlegadaAP = horaSalida.plusSeconds((trayectoria.size() - 1) * 72);
                System.out.println("       -> ruta size=" + (trayectoria != null ? trayectoria.size() : "null")
                        + ", llegada=" + horaLlegadaAP + " y la horaSalida: " + horaSalida);
                if (trayectoria == null || trayectoria.size() < 1) {
                    System.out.println("       SKIP: ruta nula o trivial");
                    continue;
                }
                if (horaLlegadaAP.isAfter(p.getPlazoMaximoEntrega())) {
                    System.out.println("       SKIP: llegada tarde ("
                            + horaLlegadaAP + " > " + p.getPlazoMaximoEntrega() + ")");
                    continue;
                }

                double neededGLP = (trayectoria.size() - 1) * ((c.getPesoVacio() + c.getGlpActualSim()) / 180.0);
                System.out.println("       neededGLP=" + neededGLP
                        + ", glpTanqueSim=" + c.getGlpTanqueSim());
                if (c.getGlpTanqueSim() < neededGLP) {
                    System.out.println("       SKIP: no hay GLP en tanque suficiente");
                    continue;
                }

                if (c.getGlpActualSim() < p.getCantidadGlp()) {
                    System.out.println("       GLP actual insuficiente ("
                            + c.getGlpActualSim() + " < " + p.getCantidadGlp()
                            + "), planificando recarga en planta más cercana");
                    Nodo finalStart = start;
                    Planta mejor = plantas.stream()
                            .min(Comparator.comparing(pl -> distance(finalStart, pl.getUbicacion())))
                            .orElse(null);

                    if (mejor == null)
                        continue;

                    Map.Entry<ArrayList<Nodo>, LocalDateTime> trayAPlanta = PathFinder.generarTrayectoria(
                            grid, start, mejor.getUbicacion(), t, p.getPlazoMaximoEntrega(), t, t,
                            p.getPlazoMaximoEntrega(), c);
                    ArrayList<Nodo> rutaPlanta = trayAPlanta.getKey();
                    LocalDateTime salidaPlanta = trayAPlanta.getValue();
                    LocalDateTime llegadaPlanta = salidaPlanta.plusSeconds((rutaPlanta.size() - 1) * 72);

                    System.out.println("       Ruta a planta size=" + rutaPlanta.size()
                            + ", llegadaPlanta=" + llegadaPlanta);

                    if (rutaPlanta == null || rutaPlanta.size() < 1
                            || llegadaPlanta.isAfter(p.getPlazoMaximoEntrega())) {
                        continue;
                    }

                    double consumoAPlanta = c.calcularConsumo(rutaPlanta.size() - 1, c.getGlpActualSim());
                    System.out.println("       consumoAPlanta=" + consumoAPlanta
                            + ", glpTanqueSim tras consumo=" + (c.getGlpTanqueSim() - consumoAPlanta));
                    if (c.getGlpTanqueSim() < consumoAPlanta)
                        continue;

                    c.setGlpTanqueSim(c.getGlpTanqueSim() - consumoAPlanta);

                    double faltante = p.getCantidadGlp() - c.getGlpActualSim();
                    double faltanteTotal = c.getCapacidadMaxima() - c.getGlpActualSim();
                    double nuevaCarga = 0;
                    if (mejor.getGlpDisponibleSim() >= faltanteTotal) {
                        nuevaCarga = c.getCapacidadMaxima();
                        mejor.setGlpDisponibleSim(mejor.getGlpDisponibleSim() - (faltanteTotal));
                    }
                    if (mejor.getGlpDisponibleSim() < faltante) {
                        continue;
                    } else {
                        nuevaCarga = Math.min(c.getGlpActualSim() + faltante, c.getCapacidadMaxima());
                        mejor.setGlpDisponibleSim(mejor.getGlpDisponibleSim() - faltante);
                    }

                    c.setGlpActualSim(nuevaCarga);
                    c.setGlpTanqueSim(25); // recargado

                    System.out.println("       GLP actual tras recarga=" + c.getGlpActualSim()
                            + ", glpTanqueSim tras recarga=" + c.getGlpTanqueSim());

                    if (mejor.getUbicacion().getPosX() == base.getPosX()
                            && mejor.getUbicacion().getPosY() == base.getPosY()) {
                        resultado = PathFinder.generarTrayectoria(
                                grid, mejor.getUbicacion(), p.getDestino(), llegadaPlanta, p.getPlazoMaximoEntrega(),
                                llegadaPlanta, llegadaPlanta.plusMinutes(15), llegadaPlanta.plusMinutes(15), c

                        );
                    } else {
                        resultado = PathFinder.generarTrayectoria(
                                grid, mejor.getUbicacion(), p.getDestino(), llegadaPlanta, p.getPlazoMaximoEntrega(),
                                llegadaPlanta, llegadaPlanta, llegadaPlanta, c);
                    }

                    trayectoria = resultado.getKey();
                    horaSalida = resultado.getValue();
                    horaLlegadaAP = horaSalida.plusSeconds((trayectoria.size() - 1) * 72);

                    if (trayectoria == null || trayectoria.size() <= 1
                            || horaLlegadaAP.isAfter(p.getPlazoMaximoEntrega())) {
                        continue;
                    }

                    double consumoAPedido = c.calcularConsumo(trayectoria.size() - 1, c.getGlpActualSim());
                    if (c.getGlpTanqueSim() < consumoAPedido)
                        continue;

                    c.setGlpTanqueSim(c.getGlpTanqueSim() - consumoAPedido);
                    c.setGlpActualSim(c.getGlpActualSim() - p.getCantidadGlp());

                    if (!c.alcanzaParaRetornar(grid, c, p.getDestino(), c.getGlpActualSim(), c.getGlpTanqueSim(),
                            horaLlegadaAP.plusMinutes(15))) {
                        continue;
                    }

                    plan.addSubRuta(
                            new SubRuta(start, mejor.getUbicacion(), null, rutaPlanta, salidaPlanta, llegadaPlanta));
                    if (mejor.getUbicacion().getPosX() == base.getPosX()
                            && mejor.getUbicacion().getPosY() == base.getPosY()) {
                        c.setGlpActualSim(c.getCapacidadMaxima());
                    }
                    start = mejor.getUbicacion();

                    mejor.setGlpDisponible(mejor.getGlpDisponibleSim());
                    /*
                     * System.out.println("       Ruta planta→pedido size=" + trayectoria.size()
                     * + ", llegadaAP=" + horaLlegadaAP);
                     * System.out.println("       consumoAPedido=" + consumoAPedido
                     * + ", glpTanqueSim tras consumoAPedido=" + (c.getGlpTanqueSim() -
                     * consumoAPedido));
                     * System.out.println("       ¿alcanza retorno? "
                     * + c.alcanzaParaRetornar(grid,c,p.getDestino(),
                     * c.getGlpActualSim(), c.getGlpTanqueSim(), horaLlegadaAP.plusMinutes(15)));
                     */

                } else {
                    c.setGlpTanqueSim(c.getGlpTanqueSim() - neededGLP);
                    c.setGlpActualSim(c.getGlpActualSim() - p.getCantidadGlp());
                    /*
                     * System.out.
                     * println("       GLP actual suficiente, consumiendo neededGLP y chequeando retorno"
                     * );
                     * System.out.println("       ¿alcanza retorno? "
                     * + c.alcanzaParaRetornar(grid,c,p.getDestino(),
                     * c.getGlpActualSim()-p.getCantidadGlp(),
                     * c.getGlpTanqueSim()-neededGLP,
                     * horaLlegadaAP));
                     */
                    if (!c.alcanzaParaRetornar(grid, c, p.getDestino(), c.getGlpActualSim(), c.getGlpTanqueSim(),
                            horaLlegadaAP.plusMinutes(15))) {
                        System.out.println("       SKIP: no puede retornar tras entrega");
                        continue;
                    }
                }
                // System.out.println("Llego a astart a detino");
                plan.addSubRuta(new SubRuta(start, p.getDestino(), p, trayectoria, horaSalida, horaLlegadaAP));

                asignado = true;
                break;
            }

            if (!asignado) {
                noAsignados.add(p);
                // System.out.println("No se pudo asignar pedido " + p.getId() + ". Su glp es: "
                // + p.getCantidadGlp()+" y su Hora de pedido fue: "+p.getHoraPedido()+" y su
                // hora de plazo maximo es: "+p.getPlazoMaximoEntrega()+" y la hora actual es:
                // "+now);
            }
        }

        // Segundo intento
        List<Pedido> reintentos = new ArrayList<>();
        for (Pedido p : noAsignados) {
            if (p.getCantidadGlp() > 25.0) {
                reintentos.addAll(dividirPedidoGrande(p));
            } else {
                reintentos.add(p);
            }
        }

        reintentos.sort(Comparator.comparing(Pedido::getPlazoMaximoEntrega));

        for (Pedido p : reintentos) {
            boolean asignado = false;
            plans.sort(Comparator.comparing(plan -> distance(plan.getCamion().getUbicacionActual(), p.getDestino())));

            for (PlanCamion plan : plans) {
                if (intentarAsignarPedido(plan, p, now)) {
                    asignado = true;
                    break;
                }
            }
            if (!asignado) {
                // System.out.println("No se pudo asignar pedido " + p.getId() + " tras dos
                // intentos. Su glp es: " + p.getCantidadGlp()+" y su Hora de pedido fue:
                // "+p.getHoraPedido()+" y su hora de plazo maximo es:
                // "+p.getPlazoMaximoEntrega());
            }
            if (!asignado && p.getCantidadGlp() <= 5.0) {
                for (PlanCamion plan : plans) {
                    if (intentarAsignarFlexible(plan, p, now)) {
                        asignado = true;
                        break;
                    }
                }
            }
            if (!asignado) {
                // System.out.println("Ni si quiera con el flexible se pudo asignar pedido " +
                // p.getId() + ". Su glp es: " + p.getCantidadGlp()+" y su Hora de pedido fue:
                // "+p.getHoraPedido()+" y su hora de plazo maximo es:
                // "+p.getPlazoMaximoEntrega()+" y la hora actual es: "+now);
            }

        }

        // Asegurar retorno a base
        for (PlanCamion plan : plans) {
            if (!plan.getSubRutas().isEmpty()) {
                // System.out.println("Aqui deberia ingresar con pedido: "+
                // plan.getSubRutas().get(plan.getSubRutas().size()-1).getPedido().getId());
                SubRuta last = plan.getSubRutas().get(plan.getSubRutas().size() - 1);
                Nodo s = last.getFin();
                LocalDateTime t = last.getHoraFin();

                if (!s.equals(base)) {
                    Map.Entry<ArrayList<Nodo>, LocalDateTime> trayRegreso = PathFinder.generarTrayectoria(
                            grid, s, base, t, t.plusHours(60), t.plusMinutes(15), t.plusMinutes(15), t.plusMinutes(15),
                            plan.getCamion());
                    ArrayList<Nodo> rutaRegreso = trayRegreso.getKey();
                    LocalDateTime salidaRegreso = trayRegreso.getValue();
                    LocalDateTime llegadaRegreso = salidaRegreso.plusSeconds((rutaRegreso.size() - 1) * 72);

                    plan.addSubRuta(new SubRuta(s, base, null, rutaRegreso, salidaRegreso, llegadaRegreso));
                }
            } else {
                Nodo ubic = plan.getCamion().getUbicacionActual();
                if (!ubic.equals(base)) {
                    Map.Entry<ArrayList<Nodo>, LocalDateTime> trayRegreso = PathFinder.generarTrayectoria(
                            grid, ubic, base, now, now.plusHours(60), now, now, now.plusHours(60), plan.getCamion());
                    ArrayList<Nodo> rutaRegreso = trayRegreso.getKey();
                    LocalDateTime salidaRegreso = trayRegreso.getValue();
                    LocalDateTime llegadaRegreso = salidaRegreso.plusSeconds((rutaRegreso.size() - 1) * 72);

                    plan.addSubRuta(new SubRuta(ubic, base, null, rutaRegreso, salidaRegreso, llegadaRegreso));
                }
            }
        }
        // for(PlanCamion p: plans){
        // System.out.println("Los planes: "+ p.getCamion().getCodigo()+" tiene "+
        // p.getSubRutas().size()+" subrutas");
        // }
        Solucion sol = new Solucion(plans, 0);
        sol.setCosto(cost(sol));
        return sol;
    }

    private boolean intentarAsignarPedido(PlanCamion plan, Pedido p, LocalDateTime now) {
        Camion c = plan.getCamion();
        if (c.getCapacidadMaxima() < p.getCantidadGlp())
            return false;

        c.setGlpActualSim(c.getGlpActual());
        c.setGlpTanqueSim(c.getGlpTanque());

        Nodo base = plantas.get(0).getUbicacion();

        Nodo nodoInicio = c.getUbicacionActual();
        LocalDateTime t = now;

        if (!plan.getSubRutas().isEmpty()) {
            SubRuta ultima = plan.getSubRutas().get(plan.getSubRutas().size() - 1);
            nodoInicio = ultima.getFin();
            t = ultima.getHoraFin();
            if (ultima.getPedido() != null) {
                t = t.plusMinutes(15);
            }
        }

        // Buscar la mejor planta (más cercana al nodoInicio)
        Nodo finalNodoInicio = nodoInicio; // Necesario para usar en el lambda
        Planta mejorPlanta = plantas.stream()
                .min(Comparator.comparing(pl -> distance(finalNodoInicio, pl.getUbicacion())))
                .orElse(null);
        if (mejorPlanta == null)
            return false;

        // Intentar trayectoria directa
        Map.Entry<ArrayList<Nodo>, LocalDateTime> resultado = PathFinder.generarTrayectoria(
                grid, nodoInicio, p.getDestino(), t, p.getPlazoMaximoEntrega(), p.getHoraPedido().plusHours(horasPlazo),
                t, p.getPlazoMaximoEntrega(), c);
        ArrayList<Nodo> trayectoria = resultado.getKey();
        LocalDateTime horaSalida = resultado.getValue();
        LocalDateTime horaLlegada = horaSalida.plusSeconds((trayectoria.size() - 1) * 72);

        if (trayectoria == null || trayectoria.size() <= 1 || horaLlegada.isAfter(p.getPlazoMaximoEntrega())) {
            return false;
        }

        double neededGLP = (trayectoria.size() - 1) * ((c.getPesoVacio() + c.getGlpActualSim()) / 180.0);
        if (c.getGlpTanqueSim() < neededGLP)
            return false;

        if (c.getGlpActualSim() < p.getCantidadGlp()) {
            // Ir primero a la planta
            Map.Entry<ArrayList<Nodo>, LocalDateTime> trayAPlanta = PathFinder.generarTrayectoria(
                    grid, nodoInicio, mejorPlanta.getUbicacion(), t, p.getPlazoMaximoEntrega(),
                    p.getHoraPedido().plusHours(horasPlazo), t, p.getPlazoMaximoEntrega(), c);
            ArrayList<Nodo> rutaPlanta = trayAPlanta.getKey();
            LocalDateTime salidaPlanta = trayAPlanta.getValue();
            LocalDateTime llegadaPlanta = salidaPlanta.plusSeconds((rutaPlanta.size() - 1) * 72);

            if (rutaPlanta == null || rutaPlanta.size() <= 1 || llegadaPlanta.isAfter(p.getPlazoMaximoEntrega())) {
                return false;
            }

            double consumoAPlanta = c.calcularConsumo(rutaPlanta.size() - 1, c.getGlpActualSim());
            if (c.getGlpTanqueSim() < consumoAPlanta)
                return false;

            double faltante = p.getCantidadGlp() - c.getGlpActualSim();
            double faltanteTotal = c.getCapacidadMaxima() - c.getGlpActualSim();
            double nuevaCarga = 0;
            if (mejorPlanta.getGlpDisponibleSim() >= faltanteTotal) {
                nuevaCarga = c.getCapacidadMaxima();
                mejorPlanta.setGlpDisponibleSim(mejorPlanta.getGlpDisponibleSim() - (faltanteTotal));
            }
            if (mejorPlanta.getGlpDisponibleSim() >= faltante) {
                nuevaCarga = Math.min(c.getGlpActualSim() + faltante, c.getCapacidadMaxima());
                mejorPlanta.setGlpDisponibleSim(mejorPlanta.getGlpDisponibleSim() - faltante);
            }

            c.setGlpActualSim(nuevaCarga);
            c.setGlpTanqueSim(25); // recargado
            if (mejorPlanta.getUbicacion().getPosX() == base.getPosX()
                    && mejorPlanta.getUbicacion().getPosY() == base.getPosY()) {
                c.setGlpActualSim(c.getCapacidadMaxima());
                resultado = PathFinder.generarTrayectoria(
                        grid, mejorPlanta.getUbicacion(), p.getDestino(),
                        llegadaPlanta, p.getPlazoMaximoEntrega(), llegadaPlanta, llegadaPlanta.plusMinutes(15),
                        llegadaPlanta.plusMinutes(15), c);
            } else {
                resultado = PathFinder.generarTrayectoria(
                        grid, mejorPlanta.getUbicacion(), p.getDestino(),
                        llegadaPlanta, p.getPlazoMaximoEntrega(), llegadaPlanta, llegadaPlanta, llegadaPlanta, c);
            }
            // Nueva trayectoria desde planta al pedido

            trayectoria = resultado.getKey();
            horaSalida = resultado.getValue();
            horaLlegada = horaSalida.plusSeconds((trayectoria.size() - 1) * 72);

            if (trayectoria == null || trayectoria.size() < 1 || horaLlegada.isAfter(p.getPlazoMaximoEntrega())) {
                return false;
            }

            double consumoAPedido = c.calcularConsumo(trayectoria.size() - 1, c.getGlpActualSim());
            if (c.getGlpTanqueSim() < consumoAPedido)
                return false;

            c.setGlpTanqueSim(c.getGlpTanqueSim() - consumoAPedido);
            c.setGlpActualSim(c.getGlpActualSim() - p.getCantidadGlp());

            if (!c.alcanzaParaRetornar(grid, c, p.getDestino(), c.getGlpActualSim(), c.getGlpTanqueSim(),
                    horaLlegada.plusMinutes(15))) {
                return false;
            }

            plan.addSubRuta(
                    new SubRuta(nodoInicio, mejorPlanta.getUbicacion(), null, rutaPlanta, salidaPlanta, llegadaPlanta));
        } else {
            c.setGlpTanqueSim(c.getGlpTanqueSim() - neededGLP);
            c.setGlpActualSim(c.getGlpActualSim() - p.getCantidadGlp());

            if (!c.alcanzaParaRetornar(grid, c, p.getDestino(), c.getGlpActualSim(), c.getGlpTanqueSim(),
                    horaLlegada)) {
                return false;
            }
        }

        plan.addSubRuta(new SubRuta(nodoInicio, p.getDestino(), p, trayectoria, horaSalida, horaLlegada));
        return true;
    }

    private List<Pedido> dividirPedidoGrande(Pedido p) {
        List<Pedido> partes = new ArrayList<>();
        double restante = p.getCantidadGlp();
        String baseId = "1000" + p.getId(); // Identificador base
        int contador = 1;

        while (restante >= 15) {
            partes.add(crearSubPedido(p, baseId + contador, 15));
            restante -= 15;
            contador++;
        }

        if (restante >= 10) {
            partes.add(crearSubPedido(p, baseId + contador, 10));
            restante -= 10;
            contador++;
        }

        if (restante >= 5) {
            partes.add(crearSubPedido(p, baseId + contador, 5));
            restante -= 5;
            contador++;
        }

        // Si queda un pequeño remanente que no es 5, 10 ni 15, lo agregamos al final
        if (restante > 0) {
            partes.add(crearSubPedido(p, baseId + contador, restante));
        }

        return partes;
    }

    private Pedido crearSubPedido(Pedido original, String nuevoId, double carga) {
        return new Pedido(
                nuevoId,
                original.getDestino(),
                original.getIdCliente(),
                carga,
                original.getHoraPedido(),
                original.getPlazoMaximoEntrega());
    }

    private boolean intentarAsignarFlexible(PlanCamion plan, Pedido p, LocalDateTime now) {
        Camion c = plan.getCamion();
        if (c.getCapacidadMaxima() < p.getCantidadGlp())
            return false;
        c.setGlpActualSim(c.getGlpActual());
        c.setGlpTanqueSim(c.getGlpTanque());
        LocalDateTime t = now;

        Nodo start = c.getUbicacionActual();
        if (!plan.getSubRutas().isEmpty()) {
            SubRuta last = plan.getSubRutas().get(plan.getSubRutas().size() - 1);
            start = last.getFin();
            t = last.getHoraFin();
            if (last.getPedido() != null) {
                t = t.plusMinutes(15);
            }
        }

        Map.Entry<ArrayList<Nodo>, LocalDateTime> resultado = PathFinder.generarTrayectoria(
                grid, start, p.getDestino(), t, p.getPlazoMaximoEntrega(), p.getHoraPedido().plusHours(horasPlazo), t,
                p.getPlazoMaximoEntrega(), c);

        ArrayList<Nodo> trayectoria = resultado.getKey();
        LocalDateTime horaSalida = resultado.getValue();
        LocalDateTime horaLlegadaAP = horaSalida.plusSeconds((trayectoria.size() - 1) * 72);

        if (trayectoria == null || trayectoria.size() <= 1 || horaLlegadaAP.isAfter(p.getPlazoMaximoEntrega())) {
            return false;
        }

        double neededGLP = (trayectoria.size() - 1) * ((c.getPesoVacio() + c.getGlpActualSim()) / 180.0);
        if (c.getGlpTanqueSim() < neededGLP)
            return false;
        if (c.getGlpActualSim() < p.getCantidadGlp())
            return false;

        // Aquí no validamos si alcanza para retornar, porque estamos flexibilizando
        // esto

        c.setGlpTanqueSim(c.getGlpTanqueSim() - neededGLP);
        c.setGlpActualSim(c.getGlpActualSim() - p.getCantidadGlp());

        plan.addSubRuta(new SubRuta(start, p.getDestino(), p, trayectoria, horaSalida, horaLlegadaAP));

        c.setGlpActualSim(c.getGlpActualSim());
        c.setGlpTanqueSim(c.getGlpTanqueSim());

        System.out.println("Asignación FLEXIBLE: Pedido " + p.getId() + " entregado con carga " + p.getCantidadGlp());
        return true;
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

    private double cost(Solucion sol) {
        // Costo original (distancias, tiempos, etc.)
        double base = sol.getCosto();
        // Número de pedidos que no están en ninguna subruta
        int undelivered = pedidos.size() - countAssigned(sol);
        // Penalización por cada pedido no asignado
        return base + EARLY_PENALTY * undelivered;
    }

    private boolean tryInsertAt(PlanCamion plan, Pedido p, LocalDateTime now, int pos) {
        // Backup de subrutas
        List<SubRuta> original = new ArrayList<>(plan.getSubRutas());
        // Calcular punto de inicio y tiempo tras las pos primeras subrutas
        Nodo start = plan.getCamion().getUbicacionActual();
        LocalDateTime t = now;
        for (int i = 0; i < pos; i++) {
            SubRuta prev = plan.getSubRutas().get(i);
            start = prev.getFin();
            t = prev.getHoraFin().plusMinutes(prev.getPedido() != null ? 15 : 0);
        }
        // Generar trayectoria
        Map.Entry<ArrayList<Nodo>, LocalDateTime> res = PathFinder.generarTrayectoria(
                grid, start, p.getDestino(), t,
                p.getPlazoMaximoEntrega(), p.getHoraPedido().plusHours(horasPlazo), t, p.getPlazoMaximoEntrega(),
                plan.getCamion());
        ArrayList<Nodo> path = res.getKey();
        LocalDateTime salida = res.getValue();
        if (path == null || path.size() <= 1) {
            plan.getSubRutas().clear();
            plan.getSubRutas().addAll(original);
            return false;
        }
        LocalDateTime llegada = salida.plusSeconds((path.size() - 1) * 72);
        if (llegada.isAfter(p.getPlazoMaximoEntrega())) {
            plan.getSubRutas().clear();
            plan.getSubRutas().addAll(original);
            return false;
        }
        // Verificar GLP
        Camion c = plan.getCamion();
        double needed = (path.size() - 1) * ((c.getPesoVacio() + c.getGlpActual()) / 180.0);
        if (c.getGlpTanque() < needed || c.getGlpActual() < p.getCantidadGlp()) {
            plan.getSubRutas().clear();
            plan.getSubRutas().addAll(original);
            return false;
        }
        // Insertar y validar plan completo
        plan.getSubRutas().add(pos, new SubRuta(start, p.getDestino(), p, path, salida, llegada));
        if (!letPlanValidate(plan, now)) {
            plan.getSubRutas().clear();
            plan.getSubRutas().addAll(original);
            return false;
        }
        return true;
    }

    private Solucion neighborSolution(Solucion base, List<Pedido> unassigned, LocalDateTime now) {

        Solucion neigh = base.clone();
        List<Pedido> faltantes = new ArrayList<>(unassigned);
        faltantes.sort(Comparator.comparing(Pedido::getPlazoMaximoEntrega));
        for (Pedido p : faltantes) {
            boolean inserted = false;
            List<PlanCamion> plans = neigh.getPlanesCamion();

            for (PlanCamion pc : plans) {

                if (!inserted) {
                    int choice = random.nextInt(3); // 0=inicio,1=medio,2=final
                    if (choice == 2) {
                        inserted = insertAtEnd(pc, p, now);
                    } else if (choice == 0) {
                        inserted = insertAtPosition(pc, p, now, 0);
                    } else {
                        int n = pc.getSubRutas().size();
                        if (n > 0) {
                            int pos = 1 + random.nextInt(n);
                            inserted = insertAtPosition(pc, p, now, pos);
                        } else {
                            inserted = insertAtEnd(pc, p, now);
                        }
                    }
                }
            }
        }

        neigh.setCosto(cost(neigh)); // esencial

        return neigh;
    }

    private boolean insertAtEnd(PlanCamion plan, Pedido p, LocalDateTime now) {
        boolean ok = insertPedido(plan, p, now);
        if (!ok)
            return false;
        SubRuta last = plan.getSubRutas().get(plan.getSubRutas().size() - 1);
        return !last.getHoraFin().isAfter(p.getPlazoMaximoEntrega());
    }

    private boolean insertAtPosition(PlanCamion plan, Pedido p, LocalDateTime now, int pos) {
        List<SubRuta> backup = new ArrayList<>(plan.getSubRutas());
        boolean ok = tryInsertAt(plan, p, now, pos);
        if (!ok)
            return false;
        boolean valid = letPlanValidate(plan, now);
        if (!valid) {
            plan.getSubRutas().clear();
            plan.getSubRutas().addAll(backup);
        }
        return valid;
    }

    private boolean insertPedido(PlanCamion plan, Pedido p, LocalDateTime now) {
        List<SubRuta> original = new ArrayList<>(plan.getSubRutas());
        Nodo start = plan.getCamion().getUbicacionActual();
        LocalDateTime t = now;
        if (!plan.getSubRutas().isEmpty()) {
            SubRuta last = plan.getSubRutas().get(plan.getSubRutas().size() - 1);
            start = last.getFin();
            t = last.getHoraFin().plusMinutes(last.getPedido() != null ? 15 : 0);
        }
        Map.Entry<ArrayList<Nodo>, LocalDateTime> res = PathFinder.generarTrayectoria(
                grid, start, p.getDestino(), t,
                p.getPlazoMaximoEntrega(), p.getHoraPedido().plusHours(horasPlazo), t, p.getPlazoMaximoEntrega(),
                plan.getCamion());
        ArrayList<Nodo> path = res.getKey();
        LocalDateTime salida = res.getValue();
        if (path == null || path.size() <= 1)
            return false;
        LocalDateTime llegada = salida.plusSeconds((path.size() - 1) * 72);
        if (llegada.isAfter(p.getPlazoMaximoEntrega()))
            return false;
        Camion c = plan.getCamion();
        double needed = (path.size() - 1) * ((c.getPesoVacio() + c.getGlpActual()) / 180.0);
        if (c.getGlpTanque() < needed || c.getGlpActual() < p.getCantidadGlp())
            return false;
        plan.getSubRutas().add(new SubRuta(start, p.getDestino(), p, path, salida, llegada));
        if (!letPlanValidate(plan, now)) {
            plan.getSubRutas().clear();
            plan.getSubRutas().addAll(original);
            return false;
        }
        return true;
    }

    private boolean letPlanValidate(PlanCamion plan, LocalDateTime now) {
        Camion c = plan.getCamion().clone();
        Nodo base = plantas.get(0).getUbicacion();
        for (SubRuta sr : plan.getSubRutas()) {
            double cons = c.calcularConsumo(sr.getTrayectoria().size() - 1, c.getGlpActual());
            if (c.getGlpTanque() < cons)
                return false;
            c.setGlpTanque(c.getGlpTanque() - cons);
            if (sr.getPedido() != null) {
                if (c.getGlpActual() < sr.getPedido().getCantidadGlp())
                    return false;
                c.setGlpActual(c.getGlpActual() - sr.getPedido().getCantidadGlp());
            }
            if (sr.getFin().equals(base) || plantas.stream().anyMatch(pl -> pl.getUbicacion().equals(sr.getFin()))) {
                c.setGlpTanque(25);
                c.setGlpActual(c.getCapacidadMaxima());
            }
        }
        SubRuta last = plan.getSubRutas().get(plan.getSubRutas().size() - 1);
        Map.Entry<ArrayList<Nodo>, LocalDateTime> ret = PathFinder.generarTrayectoria(
                grid, last.getFin(), base,
                last.getHoraFin().plusMinutes(15), last.getHoraFin().plusHours(60),
                last.getHoraFin().plusMinutes(15), last.getHoraFin().plusMinutes(15), last.getHoraFin().plusHours(60),
                c);
        ArrayList<Nodo> retorno = ret.getKey();
        return retorno != null && retorno.size() > 1;
    }

    private int countAssigned(Solucion sol) {
        return (int) sol.getPlanesCamion().stream()
                .flatMap(pl -> pl.getSubRutas().stream())
                .filter(sr -> sr.getPedido() != null).count();
    }

}
