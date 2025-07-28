package com.dp1code.routing.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import com.dp1code.routing.Model.Planta;
import com.dp1code.routing.Model.Bloqueo;
import com.dp1code.routing.Model.Mantenimiento;
import com.dp1code.routing.Model.Nodo;
import com.dp1code.routing.Model.SimulatedAnnealing;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;

import com.dp1code.routing.Model.Solucion;
import com.dp1code.routing.Model.SubRuta;
import com.dp1code.routing.Model.TimeRange;
import com.dp1code.routing.Model.Utilidades;
import java.time.Duration;
import java.time.Instant;

import com.dp1code.routing.Model.Pedido;
import com.dp1code.routing.Model.PlanCamion;
import com.dp1code.routing.Model.Camion;
import com.dp1code.routing.Model.Grid;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.cglib.core.Local;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.lang.Thread;

/**
 * Servicio que carga una vez al arranque las plantas, bloqueos y
 * mantenimientos,
 * y expone un método optimize() que ejecuta el algoritmo sobre los pedidos y
 * camiones
 * recibidos en cada petición.
 */
@Service
public class RoutingService {

    static Grid grid = new Grid(71, 51);
    static int tiermpoSalto = 3600;

    public RoutingService() {

    }

    public void iniciarSimulacionConWebSocketDiaDia(LocalDateTime fechaInicio, SimpMessagingTemplate messagingTemplate)
            throws IOException {
        LocalDateTime ahora = fechaInicio;
        LocalDateTime fechaproxima = LocalDateTime.MIN;
        while (true) {
            long startNano = System.nanoTime();
            Solucion solucion = obtenerDiaDia(fechaInicio, ahora, 1);

            if (solucion == null) {
                System.out.println("❌ Colapso detectado, se detiene la simulación.");
                Map<String, Object> colapsoMsg = new HashMap<>();
                colapsoMsg.put("colapso", true);
                messagingTemplate.convertAndSend("/topic/simulacionSemanal", colapsoMsg);
                break;
            }
            long endNano = System.nanoTime();
            double durationSeconds = (endNano - startNano) / 1_000_000_000.0;
            ArrayList<Pedido> pedidosObtenidos = new ArrayList<>(
                    solucion.getPlanesCamion().stream()
                            .flatMap(plan -> plan.getSubRutas().stream())
                            .filter(subRuta -> subRuta.getPedido() != null)
                            .map(SubRuta::getPedido)
                            .collect(Collectors.toList()));
            for (Pedido ped : pedidosObtenidos) {
                if (ped.getHoraSiguientePedido() != null && !ped.getHoraSiguientePedido().isBefore(fechaproxima)) {
                    fechaproxima = ped.getHoraSiguientePedido();
                }
            }
            // Enviar por WebSocket
            messagingTemplate.convertAndSend("/topic/operacionesDiaDia", solucion);

            if (fechaproxima != null && fechaproxima.isAfter(ahora)) {
                long millisToWait = Duration.between(LocalDateTime.now(), fechaproxima).toMillis();

                if (millisToWait > 0) {
                    System.out.println("⏳ Esperando " + millisToWait + " ms hasta la próxima planificación ("
                            + fechaproxima + ")");
                    try {
                        Thread.sleep(millisToWait);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("⛔ Hilo interrumpido durante la espera");
                        break;
                    }
                    ahora = fechaproxima;
                } else {
                    ahora = LocalDateTime.now();
                    System.out.println("⚠️ Ya se pasó la hora programada (" + fechaproxima + "), no se duerme");
                }
                
            }

            

            //ahora = ahora.plusSeconds(tiermpoSalto); // Avanzar el tiempo simulado
        }
    }

    public void iniciarSimulacionConWebSocket(LocalDateTime fechaInicio, SimpMessagingTemplate messagingTemplate)
            throws IOException {
        LocalDateTime ahora = fechaInicio;
        fechaInicio = LocalDateTime.of(2025, 5, 15, 15, 0); /////////////////////

        while (true) {
            long startNano = System.nanoTime();
            Solucion solucion = simulacionSemanal(fechaInicio, ahora);

            if (!ahora.isBefore(fechaInicio.plusHours(1))) {
                System.out.println("❌ Colapso detectado, se detiene la simulación.");
                Map<String, Object> colapsoMsg = new HashMap<>();
                colapsoMsg.put("colapso", true);
                messagingTemplate.convertAndSend("/topic/simulacionSemanal", colapsoMsg);
                break;
            }
            long endNano = System.nanoTime();
            double durationSeconds = (endNano - startNano) / 1_000_000_000.0;
            System.out.println("Tiempo de ejecución de simulacionSemanal: " + durationSeconds + " s");

            // Enviar por WebSocket
            messagingTemplate.convertAndSend("/topic/simulacionSemanal", solucion);

            ahora = ahora.plusSeconds(tiermpoSalto); // Avanzar el tiempo simulado
        }
    }

    public static Solucion simulacionSemanal(LocalDateTime fechaInput, LocalDateTime ahora) throws IOException {

        PedidoService pedidoService = new PedidoService();
        CamionService camionService = new CamionService();
        PlantaService plantaService = new PlantaService();
        boolean actualizado = true;
        System.out.println("inicioooooooooooooooooooooooooooooooo de simulacion");
        if (fechaInput.equals(ahora)) {
            System.out.println("INGRESOO A ACTUALIZAR DATOS");
            actualizado = plantaService.actualizarTodasLasPlantas();
            if (!actualizado) {
                System.out.println("Ocurrio un error: Actualizacion de todas las plantas.");
            }
            System.out.println("ACTUALIZOO PLANTAS");
            actualizado = camionService.actualizarGlpCargaTodosCamion();
            if (!actualizado) {
                System.out.println("Ocurrio un error: Actualizacion Carga Camion.");
            }
            actualizado = camionService.actualizarGlpTanqueTodosCamion();
            if (!actualizado) {
                System.out.println("Ocurrio un error: Actualizacion Tanque Camion.");
            }
            actualizado = camionService.actualizarUbicacionTodosCamion();
            if (!actualizado) {
                System.out.println("Ocurrio un error: Actualizacion Ubi Camion.");
            }
            System.out.println("ACTUALIZOO CAMIONES");
            actualizado = pedidoService.actualizarTodosPedidosANoEntregados();
            if (!actualizado) {
                System.out.println("Ocurrio un error: Actualizacion Pedidos.");
            }
            System.out.println("TERMINO DE ACTUALIZAR DATOS.");
        // ArrayList<Solucion> soluciones = new ArrayList<>();
        LocalDateTime fechaSimulada = ahora;

        // Al inicio de la simulación.
        cargarBloqueosTotales();
        ArrayList<Planta> plantas = plantaService.obtenerTodas();

        ArrayList<Camion> camiones = camionService.obtenerTodosLosCamiones();
        cargarMantenimientos("data/mantenimiento.txt", camiones);
        for (Camion c : camiones) {
            System.out.println("El camion ingresado es: " + c.getCodigo() + " y su ubi es: "
                    + c.getUbicacionActual().getPosX() + ", " + c.getUbicacionActual().getPosY()
                    + " y su glpTanque es: " + c.getGlpTanque() + " y su glpCarga es: " + c.getGlpActual());
        }
        System.out.println("La fecha Input que esta ingresando es: " + fechaInput + " y la de ahora es: " + ahora);
        ArrayList<Pedido> pedidos = pedidoService.obtenerPedidosAnteriores(fechaInput.minusSeconds(tiermpoSalto), ahora);
        System.out.println(
                "Los pedidos obtenidos son:  y son: " + pedidos.size() + " desde fechaInput menos tiempoSalto: "
                        + fechaInput.minusSeconds(tiermpoSalto) + " hasta ahora: " + ahora);
        for (Pedido p : pedidos) {
            System.out.println("El pedido con id:" + p.getId() + " y glp: " + p.getCantidadGlp()
                    + " y su ubicacion es: " + p.getDestino().getPosX() + ", " + p.getDestino().getPosY()
                    + " y la fecha hora pedido es: " + p.getHoraPedido() + " y la fecha maxima es: "
                    + p.getPlazoMaximoEntrega());
        }

        // double initialTemp = 500 + (pedidos.size() * 10);
        // double coolingRate = 0.01 - (Math.min(0.01, pedidos.size() * 0.000075));

        SimulatedAnnealing sa = new SimulatedAnnealing(1000, 0.003, 3, plantas, camiones, pedidos, grid);

        long startNano = System.nanoTime();

        Solucion mejor = sa.optimize(fechaSimulada);

        long endNano = System.nanoTime();
        double durationSeconds2 = (endNano - startNano) / 1_000_000_000.0;
        System.out.println("Tiempo de ejecución del Optimize: " + durationSeconds2 + " s");
 
        for (PlanCamion p : mejor.getPlanesCamion()) {
            System.out.println("----RETORNOOOOOOO LA SOLUCION---------");
            System.out.println("El camion: " + p.getCamion().getCodigo() + " y su ubi: "
                    + p.getCamion().getUbicacionActual().getPosX() + ", "
                    + p.getCamion().getUbicacionActual().getPosY());
            for (SubRuta sub : p.getSubRutas()) {
                if(sub.getTrayectoria().size() == 0) {
                    continue;
                }
                System.out
                        .println("El Inicio: " + sub.getHoraInicio() + ", ubi: " + sub.getTrayectoria().get(0).getPosX()
                                + ", " + sub.getTrayectoria().get(0).getPosY() + " y Fin: " + sub.getHoraFin()
                                + ", ubi: " + sub.getTrayectoria().get(sub.getTrayectoria().size() - 1).getPosX() + ", "
                                + sub.getTrayectoria().get(sub.getTrayectoria().size() - 1).getPosY()
                                + " y el size es: " + (sub.getTrayectoria().size() - 1));
                System.out.println("--");
                for (Nodo n : sub.getTrayectoria()) {
                    System.out.print("(" + n.getPosX() + ", " + n.getPosY() + "), ");
                }
                System.out.println("--");
            }
        }

        actualizarDatosBD(mejor, fechaSimulada, fechaSimulada.plusSeconds(tiermpoSalto), camiones, plantas);
        ArrayList<Pedido> pedidos2 = pedidoService.obtenerPedidosAnteriores(fechaInput.minusSeconds(tiermpoSalto),
                ahora);
        
        /*for (Pedido p : pedidos2) {
            System.out.println("Los pedidos que quedan son: " + p.getId() + " y su estado es: " + p.isEntregado());
            if (p.getPlazoMaximoEntrega().isBefore(fechaSimulada.plusSeconds(tiermpoSalto)) && !p.isEntregado()) {
                System.out.println("Colapso a las: " + fechaSimulada.plusSeconds(tiermpoSalto) + " y con el pedido: "
                        + p.getId() + "  y su fecha de plazo maximo es: " + p.getPlazoMaximoEntrega()
                        + " y la hora Pedido: " + p.getHoraPedido());
                return null;
            }
        }*/

        mejor.setPlantas(plantas);

        return mejor;
    }

    public static Solucion obtenerDiaDia(LocalDateTime fechaInput, LocalDateTime ahora, int cont) throws IOException {

        PedidoService pedidoService = new PedidoService();
        CamionService camionService = new CamionService();
        PlantaService plantaService = new PlantaService();
        boolean actualizado = true;
        System.out.println("inicioooooooooooooooooooooooooooooooo de simulacion");
        System.out.println("Esta ingresando fechaInput: " + fechaInput + " y ahora es: " + ahora);
        if (cont == 0) {
            System.out.println("INGRESOO A ACTUALIZAR DATOS");
            actualizado = plantaService.actualizarTodasLasPlantasDiaDia();
            if (!actualizado) {
                System.out.println("Ocurrio un error: Actualizacion de todas las plantas.");
            }
            System.out.println("ACTUALIZOO PLANTAS");
            actualizado = camionService.actualizarGlpCargaTodosCamionDiaDia();
            if (!actualizado) {
                System.out.println("Ocurrio un error: Actualizacion Carga Camion.");
            }
            actualizado = camionService.actualizarGlpTanqueTodosCamionDiaDia();
            if (!actualizado) {
                System.out.println("Ocurrio un error: Actualizacion Tanque Camion.");
            }
            actualizado = camionService.actualizarUbicacionTodosCamionDiaDia();
            if (!actualizado) {
                System.out.println("Ocurrio un error: Actualizacion Ubi Camion.");
            }
            System.out.println("ACTUALIZOO CAMIONES");
            actualizado = pedidoService.actualizarTodosPedidosANoEntregadosDiaDia();
            if (!actualizado) {
                System.out.println("Ocurrio un error: Actualizacion Pedidos.");
            }
            System.out.println("TERMINO DE ACTUALIZAR DATOS.");
        }
        // ArrayList<Solucion> soluciones = new ArrayList<>();
        LocalDateTime fechaSimulada = ahora;

        // Al inicio de la simulación.
        cargarBloqueosTotales();
        ArrayList<Planta> plantas = plantaService.obtenerTodasDiaDia();

        ArrayList<Camion> camiones = camionService.obtenerTodosLosCamionesDiaDia(grid);
        for (Camion c : camiones) {
            System.out.println("El camion ingresado es: " + c.getCodigo() + " y su ubi es: "
                    + c.getUbicacionActual().getPosX() + ", " + c.getUbicacionActual().getPosY()
                    + " y su glpTanque es: " + c.getGlpTanque() + " y su glpCarga es: " + c.getGlpActual());
        }
        System.out.println("La fecha Input que esta ingresando es: " + fechaInput + " y la de ahora es: " + ahora);
        System.out.println("****************************** es fecha null ********************");
        System.out.println("la fecha de entrada es: " + fechaInput + " y la fecha a comparar es: " + ahora);
        ArrayList<Pedido> pedidos = pedidoService
                .obtenerPedidosConSiguienteEnRango(fechaInput.minusSeconds(tiermpoSalto), ahora);
        LocalDateTime sigTime = pedidos.get(pedidos.size() - 1).getHoraSiguientePedido();

        for (Pedido p : pedidos) {
            System.out.println("El pedido con id:" + p.getId() + " y glp: " + p.getCantidadGlp() + " y su ubicacion es:"
                    + p.getDestino().getPosX() + ", " + p.getDestino().getPosY());
            if (p.getHoraSiguientePedido() == null)
                System.out.println("****************************** es fecha null ********************");
        }

        // ArrayList<Camion> camionesBackup = deepCopyCamiones(camiones);
        // ArrayList<Pedido> pedidosBackup = deepCopyPedidos(pedidosParaPlanificar);
        // ArrayList<Planta> plantasBackup = deepCopyPlantas(plantas);

        SimulatedAnnealing sa = new SimulatedAnnealing(5000, 0.005, 100, plantas, camiones, pedidos, grid);
        Solucion mejor = sa.optimize(fechaSimulada);
        // pedidosNoEntregados = new ArrayList<>();
        // pedidosNoEntregados = actualizarDatos(mejor,
        // fechaSimulada.plusMinutes(tiermpoSalto), camiones, plantas);
        for (PlanCamion p : mejor.getPlanesCamion()) {
            System.out.println("----RETORNOOOOOOO LA SOLUCION---------");
            System.out.println("El camion: " + p.getCamion().getCodigo() + " y su ubi: "
                    + p.getCamion().getUbicacionActual().getPosX() + ", "
                    + p.getCamion().getUbicacionActual().getPosY());
            for (SubRuta sub : p.getSubRutas()) {
                System.out
                        .println("El Inicio: " + sub.getHoraInicio() + ", ubi: " + sub.getTrayectoria().get(0).getPosX()
                                + ", " + sub.getTrayectoria().get(0).getPosY() + " y Fin: " + sub.getHoraFin()
                                + ", ubi: " + sub.getTrayectoria().get(sub.getTrayectoria().size() - 1).getPosX() + ", "
                                + sub.getTrayectoria().get(sub.getTrayectoria().size() - 1).getPosY()
                                + " y el size es: " + sub.getTrayectoria().size());
                System.out.println("--");
                for (Nodo n : sub.getTrayectoria()) {
                    System.out.print("(" + n.getPosX() + ", " + n.getPosY() + "), ");
                }
                System.out.println("--");
            }
        }
        // new Thread(() -> {

        // }).start();
        System.out.println("LA FECHA DEL PEDIDO PRÖXIMO ES:" + sigTime);
        actualizarDatosBDDiaADia(mejor, fechaSimulada, sigTime, camiones, plantas);
        for (Pedido p : pedidos) {
            if (p.getPlazoMaximoEntrega().isBefore(sigTime) && !p.isEntregado()) {
                System.out.println("Colapso a las: " + sigTime + " y con el pedido: " + p.getId()
                        + "  y su fecha de plazo maximo es: " + p.getPlazoMaximoEntrega() + " y la hora Pedido: "
                        + p.getHoraPedido());
                return null;
            }
        }
        // System.out.println("Se agrego una nueva solucion al arreglo");
        // soluciones.add(mejor);
        // fechaSimulada = fechaSimulada.plusMinutes(tiermpoSalto);

        return mejor;
    }

    public static ArrayList<Camion> deepCopyCamiones(ArrayList<Camion> original) {
        ArrayList<Camion> copia = new ArrayList<>();
        for (Camion c : original) {
            Camion nuevo = new Camion(
                    c.getCodigo(),
                    c.getTipo(),
                    c.getUbicacionActual(),
                    c.isEnRuta(),
                    c.getDisponibleDesde(),
                    c.getGlpTanque(),
                    c.getGlpActual());
            nuevo.setPesoVacio(c.getPesoVacio());
            nuevo.setCapacidadMaxima(c.getCapacidadMaxima());
            nuevo.setHoraLibre(c.getHoraLibre());

            if (c.getSubRutasExistentes() != null) {
                nuevo.setSubRutasExistentes(new ArrayList<>(c.getSubRutasExistentes()));
            }
            if (c.getMantenimientos() != null) {
                nuevo.setMantenimientos(new ArrayList<>(c.getMantenimientos()));
            }

            copia.add(nuevo);
        }
        return copia;
    }

    public static ArrayList<Pedido> deepCopyPedidos(ArrayList<Pedido> original) {
        ArrayList<Pedido> copia = new ArrayList<>();
        for (Pedido p : original) {
            Pedido nuevo = new Pedido(
                    p.getId(),
                    p.getDestino(),
                    p.getIdCliente(),
                    p.getCantidadGlp(),
                    p.getHoraPedido(),
                    p.getPlazoMaximoEntrega());
            nuevo.setTiempoDescarga(p.getTiempoDescarga());
            nuevo.setEntregado(p.isEntregado());
            copia.add(nuevo);
        }
        return copia;
    }

    public static ArrayList<Planta> deepCopyPlantas(ArrayList<Planta> original) {
        ArrayList<Planta> copia = new ArrayList<>();
        for (Planta p : original) {
            Planta nuevo = new Planta(p.getId(), p.getTipo(), p.getUbicacion());
            nuevo.setCapacidadMaxima(p.getCapacidadMaxima());
            nuevo.setGlpDisponible(p.getGlpDisponible());
            nuevo.setSiguienteRecarga(p.getSiguienteRecarga());
            nuevo.setIntervaloRecarga(p.getIntervaloRecarga());
            copia.add(nuevo);
        }
        return copia;
    }

    private static ArrayList<Pedido> actualizarDatos(Solucion solucion, LocalDateTime fechaSimulada,
            ArrayList<Camion> camiones, ArrayList<Planta> plantas) {
        ArrayList<Pedido> pedidosNoEntregados = new ArrayList<>();
        System.out.println("Se ingresa a actualizar datos a la fecha: " + fechaSimulada);
        for (PlanCamion plan : solucion.getPlanesCamion()) {
            if (plan.getSubRutas().size() != 0) {
                Camion c = new Camion();
                for (Camion camion : camiones) {
                    if (camion.getCodigo() == plan.getCamion().getCodigo()) {
                        c = camion;
                    }
                }
                // Primero verifiquemos si ya termino la ultima subRuta de este plan.
                SubRuta last = plan.getSubRutas().get(plan.getSubRutas().size() - 1);
                if (!last.getHoraFin().isAfter(fechaSimulada)) {
                    c.setUbicacionActual(last.getTrayectoria().get(last.getTrayectoria().size() - 1));
                    for (SubRuta sub : plan.getSubRutas()) {
                        double glpConsumida = c.calcularConsumo(sub.getTrayectoria().size() - 1);
                        c.setGlpTanque(c.getGlpTanque() - glpConsumida);
                        if (sub.getPedido() != null) {
                            double cargaEntregada = sub.getPedido().getCantidadGlp();
                            c.setGlpActual(c.getGlpActual() - cargaEntregada);

                            sub.getPedido().setEntregado(true);
                            System.out.println("Se entrego el pedido: " + sub.getPedido().getId() + " con carga de "
                                    + sub.getPedido().getCantidadGlp());
                        }
                        if (Utilidades.esPlantaSecundaria(sub.getTrayectoria().get(sub.getTrayectoria().size() - 1),
                                plantas)) {
                            Planta planta = Utilidades.obtenerPlanta(sub.getTrayectoria().get(sub.getTrayectoria().size() - 1), plantas);
                            double glpFaltante = c.getCapacidadMaxima() - c.getGlpActual();
                            planta.setGlpDisponible(planta.getGlpDisponible() - glpFaltante);

                        }
                        if (Utilidades.esPlantaPrincipal(sub.getTrayectoria().get(sub.getTrayectoria().size() - 1),
                                plantas)) {
                            c.setGlpTanque(25);
                            c.setGlpActual(c.getCapacidadMaxima());
                        }

                    }
                    continue;
                }
                // Luego verifiquemos si aun no comienza.
                if (!plan.getSubRutas().get(0).getHoraInicio().isBefore(fechaSimulada)) {
                    for (SubRuta sub : plan.getSubRutas()) {
                        if (sub.getPedido() != null) {
                            pedidosNoEntregados.add(sub.getPedido());
                        }
                    }
                    continue;
                }
                // Luego vemos si es que esta en ruta. Y buscamos en que subRuta esta, para ir
                // checandolo desde ahí.
                for (SubRuta sub : plan.getSubRutas()) {
                    // Si ya paso esa subRuta(A-> B).
                    if (!sub.getHoraFin().isAfter(fechaSimulada) && !sub.getTrayectoria().isEmpty()) {
                        double glpConsumida = c.calcularConsumo(sub.getTrayectoria().size() - 1);
                        c.setGlpTanque(c.getGlpTanque() - glpConsumida);
                        if (sub.getPedido() != null) {
                            sub.getPedido().setEntregado(true);
                            System.out.println("Se entrego el pedido: " + sub.getPedido().getId() + " con carga de "
                                    + sub.getPedido().getCantidadGlp());
                        }
                        if (Utilidades.esPlantaSecundaria(sub.getTrayectoria().get(0), plantas)) {
                            Planta planta = Utilidades.obtenerPlanta(sub.getTrayectoria().get(sub.getTrayectoria().size() - 1), plantas);
                            double glpFaltante = c.getCapacidadMaxima() - c.getGlpTanque();
                            planta.setGlpDisponible(planta.getGlpDisponible() - glpFaltante);
                        }
                        if (Utilidades.esPlantaPrincipal(sub.getTrayectoria().get(0), plantas)) {
                            c.setGlpTanque(25);
                            c.setGlpActual(c.getCapacidadMaxima());
                        }
                    } else if (!sub.getHoraInicio().isBefore(fechaSimulada)) {// Si aun no llego a esa SubRuta
                        if (sub.getPedido() != null) {
                            pedidosNoEntregados.add(sub.getPedido());
                        }
                    } else {// Si esta en plena SubRuta
                        if (sub.getPedido() != null) {
                            pedidosNoEntregados.add(sub.getPedido());
                        }
                        for (int i = 0; i < sub.getTrayectoria().size() - 1; i++) {
                            LocalDateTime tiempoParcial = sub.getTiemposNodo().get(i);
                            LocalDateTime tiempoParcialSiguiente = sub.getTiemposNodo().get(i + 1);

                            if (fechaSimulada.isAfter(tiempoParcial)
                                    && fechaSimulada.isBefore(tiempoParcialSiguiente)) {
                                double distancia = i;
                                c.setUbicacionActual(sub.getTrayectoria().get(i));
                                c.setGlpTanque(c.getGlpTanque() - c.calcularConsumo(distancia));
                            }
                        }

                    }
                }
            }
        }
        return pedidosNoEntregados;
    }

    private static void actualizarDatosBD(Solucion solucion, LocalDateTime fechaSimuladaAnterior,
            LocalDateTime fechaSimulada, ArrayList<Camion> camiones, ArrayList<Planta> plantas) {
        CamionService camionService = new CamionService();
        PedidoService pedidoService = new PedidoService();
        PlantaService plantaService = new PlantaService();
        boolean actualizado = true;
        System.out.println("Se ingresa a actualizar datos a la fecha: " + fechaSimulada);
        for (PlanCamion plan : solucion.getPlanesCamion()) {
            if (plan.getSubRutas().size() != 0) {
                Camion c = new Camion();
                

                for (Camion camion : camiones) {
                    if (camion.getCodigo() == plan.getCamion().getCodigo()) {
                        c = camion;
                    }
                }
                System.out.println("El camion con codigo: " + c.getCodigo() + " ingresa en el tanque con: " + c.getGlpTanque() + " glp en el tanque. Y glpActual: " + c.getGlpActual());
                // Primero verifiquemos si ya termino la ultima subRuta de este plan.
                SubRuta last = plan.getSubRutas().get(plan.getSubRutas().size() - 1);
                if (!last.getHoraFin().isAfter(fechaSimulada)) {
                    c.setUbicacionActual(last.getTrayectoria().get(last.getTrayectoria().size() - 1));

                    for (SubRuta sub : plan.getSubRutas()) {
                        double glpConsumida = c.calcularConsumo(sub.getTrayectoria().size() - 1);
                        c.setGlpTanque(c.getGlpTanque() - glpConsumida);

                        if (sub.getPedido() != null) {
                            double cargaEntregada = sub.getPedido().getCantidadGlp();
                            c.setGlpActual(c.getGlpActual() - cargaEntregada);

                            sub.getPedido().setEntregado(true);
                            sub.getPedido().setTiempoEntrega(sub.getHoraFin());
                            if (sub.getPedido().getId().startsWith("1000") && sub.getPedido().getId().length() > 8) {
                                actualizado = pedidoService.actualizarEstadoEntregadoPositivo(
                                        sub.getPedido().getId().substring(4, sub.getPedido().getId().length() - 1),
                                        sub.getPedido().getTiempoEntrega());
                            } else {
                                actualizado = pedidoService.actualizarEstadoEntregadoPositivo(sub.getPedido().getId(),
                                        sub.getPedido().getTiempoEntrega());
                            }

                            c.setNumPedidosAtendidos(c.getNumPedidosAtendidos() + 1);
                            if (!actualizado) {
                                System.out.println("Ocurrio un error: Pedido");
                            }
                            System.out.println("Se entrego el pedido: " + sub.getPedido().getId() + " con carga de "
                                    + sub.getPedido().getCantidadGlp());
                        }
                        if (Utilidades.esPlantaSecundaria(sub.getTrayectoria().get(sub.getTrayectoria().size() - 1),
                                plantas)) {
                            Planta planta = Utilidades.obtenerPlanta(sub.getTrayectoria().get(sub.getTrayectoria().size() - 1), plantas);
                            double glpFaltante = c.getCapacidadMaxima() - c.getGlpActual();
                            planta.setGlpDisponible(planta.getGlpDisponible() - glpFaltante);
                            c.setGlpActual(c.getCapacidadMaxima());
                            c.setGlpTanque(25);
                        }
                        if (Utilidades.esPlantaPrincipal(sub.getTrayectoria().get(sub.getTrayectoria().size() - 1),
                                plantas)) {
                            c.setGlpTanque(25);

                            c.setGlpActual(c.getCapacidadMaxima());

                        }

                    }

                    continue;
                }
                // Luego verifiquemos si aun no comienza.
                /*
                 * if(!plan.getSubRutas().get(0).getHoraInicio().isBefore(fechaSimulada)){
                 * for(SubRuta sub: plan.getSubRutas()){
                 * if(sub.getPedido()!=null){
                 * 
                 * }
                 * }
                 * continue;
                 * }
                 */
                // Luego vemos si es que esta en ruta. Y buscamos en que subRuta esta, para ir
                // checandolo desde ahí.
                for (SubRuta sub : plan.getSubRutas()) {
                    // Si ya paso esa subRuta(A-> B).
                    
                    if (!sub.getHoraFin().isAfter(fechaSimulada) && !sub.getTrayectoria().isEmpty()) {
                        System.out.println("Ingreso a paso la subRuta: "+ sub.getHoraInicio() + " a " + sub.getHoraFin());
                        double glpConsumida = c.calcularConsumo(sub.getTrayectoria().size() - 1);
                        c.setGlpTanque(c.getGlpTanque() - glpConsumida);
                        c.setUbicacionActual(sub.getTrayectoria().get(sub.getTrayectoria().size() - 1));

                        if (sub.getPedido() != null) {
                            double cargaEntregada = sub.getPedido().getCantidadGlp();
                            c.setGlpActual(c.getGlpActual() - cargaEntregada);
                            sub.getPedido().setEntregado(true);
                            sub.getPedido().setTiempoEntrega(sub.getHoraFin());
                            if (sub.getPedido().getId().startsWith("1000") && sub.getPedido().getId().length() > 8) {
                                actualizado = pedidoService.actualizarEstadoEntregadoPositivo(
                                        sub.getPedido().getId().substring(4, sub.getPedido().getId().length() - 1),
                                        sub.getPedido().getTiempoEntrega());
                            } else {
                                actualizado = pedidoService.actualizarEstadoEntregadoPositivo(sub.getPedido().getId(),
                                        sub.getPedido().getTiempoEntrega());
                            }

                            c.setNumPedidosAtendidos(c.getNumPedidosAtendidos() + 1);
                            if (!actualizado) {
                                System.out.println("Ocurrio un error: Pedido");
                            }
                            System.out.println("Se entrego el pedido: " + sub.getPedido().getId() + " con carga de "
                                    + sub.getPedido().getCantidadGlp());
                        }


                        if (Utilidades.esPlantaSecundaria(sub.getTrayectoria().get(sub.getTrayectoria().size() - 1),
                                plantas)) {
                            System.out.println("Ingreso a actualizar a una planta Secundaria");
                            Planta planta = Utilidades.obtenerPlanta(sub.getTrayectoria().get(sub.getTrayectoria().size() - 1), plantas);
                            double glpFaltante = c.getCapacidadMaxima() - c.getGlpActual();
                            c.setGlpTanque(25);
                            planta.setGlpDisponible(planta.getGlpDisponible() - glpFaltante);
                            c.setGlpActual(c.getCapacidadMaxima());

                        }
                        if (Utilidades.esPlantaPrincipal(sub.getTrayectoria().get(sub.getTrayectoria().size() - 1),
                                plantas)) {
                            System.out.println("Ingreso a actualizar a una planta Principal");
                            c.setGlpTanque(25);
                            c.setGlpActual(c.getCapacidadMaxima());

                        }
                    }
                    /*
                     * else if (!sub.getHoraInicio().isBefore(fechaSimulada)){//Si aun no llego a
                     * esa SubRuta
                     * if(sub.getPedido()!=null){
                     * 
                     * }
                     * }
                     */
                    else {// Si esta en plena SubRuta
                          // if(sub.getPedido()!=null){

                        // }
                        for (int i = 0; i < sub.getTrayectoria().size() - 1; i++) {
                            LocalDateTime tiempoParcial = sub.getTiemposNodo().get(i);
                            LocalDateTime tiempoParcialSiguiente = sub.getTiemposNodo().get(i + 1);

                            if (!fechaSimulada.isBefore(tiempoParcial)
                                    && fechaSimulada.isBefore(tiempoParcialSiguiente)) {

                                double distancia = i;

                                System.out.println("Entrooo aquiiiiiiiiiiiiiiiiiiiiiiii y el camion: " + c.getCodigo()
                                        + " esta en la ubicacion: " + c.getUbicacionActual());
                                c.setUbicacionActual(sub.getTrayectoria().get(i + 1));
                                c.setGlpTanque(c.getGlpTanque() - c.calcularConsumo(distancia));

                            }
                        }

                    }
                }
            }
        }
        if (fechaSimuladaAnterior.toLocalDate().isBefore(fechaSimulada.toLocalDate())) {
            for (Planta planta : plantas) {
                if (Utilidades.esPlantaPrincipal(planta.getUbicacion(), plantas)) {
                    planta.setGlpDisponible(10000);
                } else {
                    planta.setGlpDisponible(60);
                }
            }
        }

        LocalDateTime inicioBD = LocalDateTime.now();
        System.out.println("COMENZOOOOOOO CON LA BD:");
        actualizado = true;
        try (Connection conn = DatabaseService.getConnection()) {
            conn.setAutoCommit(false);

            actualizado = plantaService.actualizarPlantasBatch(plantas, conn);

            if (!actualizado)
                System.out.println("Error al actualizar plantas");

            actualizado = camionService.actualizarCamionesBatch(camiones, conn, fechaSimulada);
            for(Camion camion : camiones){
                if(camion.isDisponiblePorMantenimiento(fechaSimulada)){
                    camion.setEstado("L");
                } else {
                    camion.setEstado("ND");
                }
            }
            if (!actualizado)
                System.out.println("Error al actualizar camiones");

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            // Optional: rollback
        }
        LocalDateTime finBD = LocalDateTime.now();
        Duration duracion = Duration.between(inicioBD, finBD);
        System.out.println("Tiempo en actualizar BD: " + duracion.toMillis() + " ms");
        System.out.println("TERMINOOOO DE ACTUALIZAR LA BD");
    }

    private static void actualizarDatosBDDiaADia(Solucion solucion, LocalDateTime fechaSimuladaAnterior,
            LocalDateTime fechaSimulada, ArrayList<Camion> camiones, ArrayList<Planta> plantas) {
        CamionService camionService = new CamionService();
        PedidoService pedidoService = new PedidoService();
        PlantaService plantaService = new PlantaService();
        boolean actualizado = true;
        System.out.println("Se ingresa a actualizar datos a la fecha: " + fechaSimulada);
        for (PlanCamion plan : solucion.getPlanesCamion()) {
            if (plan.getSubRutas().size() != 0) {
                Camion c = new Camion();

                for (Camion camion : camiones) {
                    if (camion.getCodigo() == plan.getCamion().getCodigo()) {
                        c = camion;
                    }
                }

                // Primero verifiquemos si ya termino la ultima subRuta de este plan.
                SubRuta last = plan.getSubRutas().get(plan.getSubRutas().size() - 1);
                if (!last.getHoraFin().isAfter(fechaSimulada)) {
                    c.setUbicacionActual(last.getTrayectoria().get(last.getTrayectoria().size() - 1));

                    for (SubRuta sub : plan.getSubRutas()) {
                        double glpConsumida = c.calcularConsumo(sub.getTrayectoria().size() - 1);
                        c.setGlpTanque(c.getGlpTanque() - glpConsumida);

                        if (sub.getPedido() != null) {
                            double cargaEntregada = sub.getPedido().getCantidadGlp();
                            c.setGlpActual(c.getGlpActual() - cargaEntregada);

                            sub.getPedido().setEntregado(true);
                            System.out.println("Se entrego el pedido: " + sub.getPedido().getId() + " con carga de "
                                    + sub.getPedido().getCantidadGlp());
                            sub.getPedido().setTiempoEntrega(sub.getHoraFin());
                            if (sub.getPedido().getId().startsWith("1000") && sub.getPedido().getId().length() > 8) {
                                sub.getPedido().setId(
                                        sub.getPedido().getId().substring(4, sub.getPedido().getId().length() - 1));
                                ;
                            }
                            actualizado = pedidoService
                                    .actualizarEstadoEntregadoPositivoDiaDia(sub.getPedido().getId(),
                                            sub.getPedido().getTiempoEntrega());

                            c.setNumPedidosAtendidos(c.getNumPedidosAtendidos() + 1);
                            if (!actualizado) {
                                System.out.println("Ocurrio un error: Pedido");
                            }
                            System.out.println("Se entrego el pedido: " + sub.getPedido().getId() + " con carga de "
                                    + sub.getPedido().getCantidadGlp());
                        }
                        if (Utilidades.esPlantaSecundaria(sub.getTrayectoria().get(sub.getTrayectoria().size() - 1),
                                plantas)) {
                            Planta planta = Utilidades.obtenerPlanta(sub.getTrayectoria().get(sub.getTrayectoria().size() - 1), plantas);
                            double glpFaltante = c.getCapacidadMaxima() - c.getGlpActual();
                            planta.setGlpDisponible(planta.getGlpDisponible() - glpFaltante);
                            c.setGlpActual(c.getCapacidadMaxima());
                            c.setGlpTanque(25);
                        }
                        if (Utilidades.esPlantaPrincipal(sub.getTrayectoria().get(sub.getTrayectoria().size() - 1),
                                plantas)) {
                            c.setGlpTanque(25);

                            c.setGlpActual(c.getCapacidadMaxima());

                        }

                    }

                    continue;
                }
                // Luego verifiquemos si aun no comienza.
                /*
                 * if(!plan.getSubRutas().get(0).getHoraInicio().isBefore(fechaSimulada)){
                 * for(SubRuta sub: plan.getSubRutas()){
                 * if(sub.getPedido()!=null){
                 * 
                 * }
                 * }
                 * continue;
                 * }
                 */
                // Luego vemos si es que esta en ruta. Y buscamos en que subRuta esta, para ir
                // checandolo desde ahí.
                for (SubRuta sub : plan.getSubRutas()) {
                    // Si ya paso esa subRuta(A-> B).

                    if (!sub.getHoraFin().isAfter(fechaSimulada) && !sub.getTrayectoria().isEmpty()) {
                        double glpConsumida = c.calcularConsumo(sub.getTrayectoria().size() - 1);
                        c.setGlpTanque(c.getGlpTanque() - glpConsumida);

                        if (sub.getPedido() != null) {
                            double cargaEntregada = sub.getPedido().getCantidadGlp();
                            c.setGlpActual(c.getGlpActual() - cargaEntregada);
                            sub.getPedido().setEntregado(true);
                            System.out.println("Se entrego el pedido: " + sub.getPedido().getId() + " con carga de "
                                    + sub.getPedido().getCantidadGlp());
                            sub.getPedido().setTiempoEntrega(sub.getHoraFin());
                            // ACTUALIZAR PEDIDO
                            if (sub.getPedido().getId().startsWith("1000") && sub.getPedido().getId().length() > 8) {
                                sub.getPedido().setId(
                                        sub.getPedido().getId().substring(4, sub.getPedido().getId().length() - 1));
                                ;
                            }
                            actualizado = pedidoService
                                    .actualizarEstadoEntregadoPositivoDiaDia(sub.getPedido().getId(),
                                            sub.getPedido().getTiempoEntrega());
                            c.setNumPedidosAtendidos(c.getNumPedidosAtendidos() + 1);

                            if (!actualizado) {
                                System.out.println("Ocurrio un error: Pedido");
                            }

                        }
                        if (Utilidades.esPlantaSecundaria(sub.getTrayectoria().get(sub.getTrayectoria().size() - 1),
                                plantas)) {
                            Planta planta = Utilidades.obtenerPlanta(sub.getTrayectoria().get(sub.getTrayectoria().size() - 1), plantas);
                            double glpFaltante = c.getCapacidadMaxima() - c.getGlpActual();
                            c.setGlpTanque(25);
                            planta.setGlpDisponible(planta.getGlpDisponible() - glpFaltante);
                            c.setGlpActual(c.getCapacidadMaxima());

                        }
                        if (Utilidades.esPlantaPrincipal(sub.getTrayectoria().get(sub.getTrayectoria().size() - 1),
                                plantas)) {
                            c.setGlpTanque(25);

                            c.setGlpActual(c.getCapacidadMaxima());

                        }
                    }
                    /*
                     * else if (!sub.getHoraInicio().isBefore(fechaSimulada)){//Si aun no llego a
                     * esa SubRuta
                     * if(sub.getPedido()!=null){
                     * 
                     * }
                     * }
                     */
                    else {// Si esta en plena SubRuta
                          // if(sub.getPedido()!=null){

                        // }
                        for (int i = 0; i < sub.getTrayectoria().size() - 1; i++) {
                            LocalDateTime tiempoParcial = sub.getTiemposNodo().get(i);
                            LocalDateTime tiempoParcialSiguiente = sub.getTiemposNodo().get(i + 1);

                            if (fechaSimulada.isAfter(tiempoParcial)
                                    && fechaSimulada.isBefore(tiempoParcialSiguiente)) {
                                double distancia = i;
                                c.setUbicacionActual(sub.getTrayectoria().get(i));

                                c.setGlpTanque(c.getGlpTanque() - c.calcularConsumo(distancia));

                            }
                        }

                    }
                }
            }
        }
        if (fechaSimuladaAnterior.toLocalDate().isBefore(fechaSimulada.toLocalDate())) {
            for (Planta planta : plantas) {
                if (Utilidades.esPlantaPrincipal(planta.getUbicacion(), plantas)) {
                    planta.setGlpDisponible(10000);
                } else {
                    planta.setGlpDisponible(60);
                }
            }
        }

        LocalDateTime inicioBD = LocalDateTime.now();
        System.out.println("COMENZOOOOOOO CON LA BD:");
        actualizado = true;
        try (Connection conn = DatabaseService.getConnection()) {
            conn.setAutoCommit(false);

            actualizado = plantaService.actualizarPlantasBatchDiaDia(plantas, conn);
            if (!actualizado)
                System.out.println("Error al actualizar plantas");

            actualizado = camionService.actualizarCamionesBatchDiaADia(camiones, conn);
            if (!actualizado)
                System.out.println("Error al actualizar camiones");

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            // Optional: rollback
        }
        LocalDateTime finBD = LocalDateTime.now();
        Duration duracion = Duration.between(inicioBD, finBD);
        System.out.println("Tiempo en actualizar BD: " + duracion.toMillis() + " ms");
        System.out.println("TERMINOOOO DE ACTUALIZAR LA BD");
    }

    static void cargarBloqueosTotales() throws IOException {
        cargarBloqueos("data/bloqueosEnero.txt", Month.JANUARY);
        cargarBloqueos("data/bloqueosFebrero.txt", Month.FEBRUARY);
        cargarBloqueos("data/bloqueosMarzo.txt", Month.MARCH);
        cargarBloqueos("data/bloqueosAbril.txt", Month.APRIL);
        cargarBloqueos("data/bloqueosMayo.txt", Month.MAY);
        cargarBloqueos("data/bloqueosJunio.txt", Month.JUNE);
        cargarBloqueos("data/bloqueosJulio.txt", Month.JULY);
        cargarBloqueos("data/bloqueosAgosto.txt", Month.AUGUST);
        cargarBloqueos("data/bloqueosSeptiembre.txt", Month.SEPTEMBER);
        cargarBloqueos("data/bloqueosOctubre.txt", Month.OCTOBER);
        cargarBloqueos("data/bloqueosNoviembre.txt", Month.NOVEMBER);
        cargarBloqueos("data/bloqueosDiciembre.txt", Month.DECEMBER);
    }

    static void cargarBloqueos(String archivo, Month mes) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String linea;

        while ((linea = br.readLine()) != null) {

            if (linea.trim().isEmpty() || linea.trim().startsWith("#")) {
                continue;
            }

            String[] partes = linea.split(":");
            if (partes.length != 2) {
                System.err.println("Formato inválido en línea: " + linea);
                continue;
            }

            String[] tiempos = partes[0].split("-");
            if (tiempos.length != 2) {
                System.err.println("Formato de tiempo inválido en línea: " + linea);
                continue;
            }

            LocalDateTime inicio = parsearFechaHora(tiempos[0], mes);
            LocalDateTime fin = parsearFechaHora(tiempos[1],  mes);

            String[] coordenadas = partes[1].split(",");
            if (coordenadas.length % 2 != 0) {
                System.err.println("Número impar de coordenadas en línea: " + linea);
                continue;
            }

            for (int i = 0; i < coordenadas.length - 2; i += 2) {
                try {
                    int x1 = Integer.parseInt(coordenadas[i].trim());
                    int y1 = Integer.parseInt(coordenadas[i + 1].trim());
                    int x2 = Integer.parseInt(coordenadas[i + 2].trim());
                    int y2 = Integer.parseInt(coordenadas[i + 3].trim());

                    List<Nodo> intermedios = Utilidades.obtenerNodosIntermedios(x1, y1, x2, y2, grid);
                    for (Nodo nodo : intermedios) {
                        nodo.agregarBloqueo(inicio, fin);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Coordenada inválida en línea: " + linea);
                }
            }
        }
        br.close();
    }


    public static ArrayList<Pedido> cargarPedidosCompletos(String filePath, LocalDateTime ahora) throws IOException {
        ArrayList pedidos = new ArrayList<>();
        Path path = Paths.get(filePath);
        LocalDateTime base = ahora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        int i = 0;
        for (String line : Files.readAllLines(path)) {
            if (line.isBlank())
                continue;

            String[] parts = line.split(":");

            String[] ts = parts[0].split("[dhm]");
            int d = Integer.parseInt(ts[0]);
            int h = Integer.parseInt(ts[1]);
            int m = Integer.parseInt(ts[2]);
            LocalDateTime horaPedido = base.plusDays(d - 1).plusHours(h).plusMinutes(m);
            String[] vals = parts[1].split(",");
            int x = Integer.parseInt(vals[0]);
            int y = Integer.parseInt(vals[1]);
            String id = vals[2];
            int m3 = Integer.parseInt(vals[3].replace("m3", ""));
            int hLim = Integer.parseInt(vals[4].replace("h", ""));
            LocalDateTime plazoMax = horaPedido.plusHours(hLim);
            i++;
            pedidos.add(new Pedido(String.valueOf(i), grid.getNodoAt(x, y), id, m3, horaPedido, plazoMax));
        }
        return pedidos;
    }

    public static ArrayList<Pedido> cargarPedidosParaPlanificar(String filePath, LocalDateTime ahora,
            ArrayList<Pedido> pedidosNoEntregadosAnteriormente, ArrayList<Pedido> pedidos) throws IOException {
        ArrayList<Pedido> pedidosNuevos = obtenerPedidos(ahora, pedidos);

        for (Pedido pedido : pedidosNuevos) {
            boolean yaExiste = false;

            for (Pedido pedidoPrevio : pedidosNoEntregadosAnteriormente) {
                if (pedido.getId() == pedidoPrevio.getId()) {
                    yaExiste = true;
                    break; // Salimos del segundo bucle, ya sabemos que existe
                }
            }

            if (!yaExiste) {
                // System.out.println("Pedido nuevo: " + pedido.getId());
                pedidosNoEntregadosAnteriormente.add(pedido);
            }
        }

        return pedidosNoEntregadosAnteriormente;
    }

    public static ArrayList<Pedido> obtenerPedidos(LocalDateTime ahora, ArrayList<Pedido> pedidos) throws IOException {
        ArrayList<Pedido> pedidosAObtener = new ArrayList<>();
        for (Pedido p : pedidos) {
            if (!p.getHoraPedido().isAfter(ahora) && !p.getHoraPedido().isBefore(ahora.minusMinutes(tiermpoSalto))) {
                pedidosAObtener.add(p);
            }
        }
        return pedidosAObtener;
    }

    public static ArrayList<Pedido> cargarPedidosSegmentado(String filePath, LocalDateTime ahora) throws IOException {
        ArrayList<Pedido> pedidos = new ArrayList<>();
        Path path = Paths.get(filePath);

        // Base del mes de simulación: primer día a las 00:00
        LocalDateTime base = ahora
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        int i = 0;
        for (String line : Files.readAllLines(path)) {
            i++;
            if (line.isBlank())
                continue;
            String[] parts = line.split(":");
            // Tiempo de llegada
            String[] ts = parts[0].split("[dhm]");
            int d = Integer.parseInt(ts[0]);
            int h = Integer.parseInt(ts[1]);
            int m = Integer.parseInt(ts[2]);
            LocalDateTime horaPedido = base.plusDays(d - 1).plusHours(h).plusMinutes(m);

            if (!horaPedido.isAfter(ahora) && !horaPedido.isBefore(ahora.minusMinutes(tiermpoSalto))) {
                // Datos restantes

                String[] vals = parts[1].split(",");
                int x = Integer.parseInt(vals[0]);
                int y = Integer.parseInt(vals[1]);
                String id = vals[2];
                int m3 = Integer.parseInt(vals[3].replace("m3", ""));
                int hLim = Integer.parseInt(vals[4].replace("h", ""));
                LocalDateTime plazoMax = horaPedido.plusHours(hLim);
                Pedido p = new Pedido(String.valueOf(i), grid.getNodoAt(x, y), id, m3, horaPedido, plazoMax);
                pedidos.add(p);
            }
        }
        return pedidos;
    }

    public static ArrayList<Planta> obtenerPlantas() throws IOException {
        ArrayList<Planta> plantas = new ArrayList<>();

        Planta plantaPrincipal = new Planta(1, "PRINCIPAL", grid.getNodoAt(12, 8));
        Planta plantaSecundaria1 = new Planta(2, "SECUNDARIA", grid.getNodoAt(42, 42));
        Planta plantaSecundaria2 = new Planta(3, "SECUNDARIA", grid.getNodoAt(63, 8));

        plantas.add(plantaPrincipal);
        plantas.add(plantaSecundaria1);
        plantas.add(plantaSecundaria2);
        return plantas;
    }

    public static ArrayList<Camion> cargarCamiones(String filePath, LocalDateTime ahora) throws IOException {
        ArrayList<Camion> camiones = new ArrayList<>();
        Map<String, Integer> count = new HashMap<>();
        Path path = Paths.get(filePath);
        for (String line : Files.readAllLines(path)) {
            if (line.isBlank())
                continue;
            String[] parts = line.split(",");
            String tipo = parts[0];
            double tanqueActual = Double.parseDouble(parts[1]);
            double cargaActual = Double.parseDouble(parts[2]);
            int x = Integer.parseInt(parts[3]);
            int y = Integer.parseInt(parts[4]);
            // Contador para código
            int idx = count.getOrDefault(tipo, 0) + 1;
            count.put(tipo, idx);
            String codigo = String.format("%s%02d", tipo, idx);

            Nodo ubic = grid.getNodoAt(x, y);
            Camion c = new Camion(codigo, tipo, ubic, false, ahora, tanqueActual, cargaActual);
            camiones.add(c);
        }
        return camiones;
    }

    public static ArrayList<Bloqueo> obtenerBloqueosTotales() throws IOException {
        ArrayList<Bloqueo> bloqueos = new ArrayList<>();
        bloqueos.addAll(obtenerBloqueos("data/bloqueosEnero.txt", Month.JANUARY));
        bloqueos.addAll(obtenerBloqueos("data/bloqueosFebrero.txt", Month.FEBRUARY));
        bloqueos.addAll(obtenerBloqueos("data/bloqueosMarzo.txt", Month.MARCH));
        bloqueos.addAll(obtenerBloqueos("data/bloqueosAbril.txt", Month.APRIL));
        bloqueos.addAll(obtenerBloqueos("data/bloqueosMayo.txt", Month.MAY));
        bloqueos.addAll(obtenerBloqueos("data/bloqueosJunio.txt", Month.JUNE));
        bloqueos.addAll(obtenerBloqueos("data/bloqueosJulio.txt", Month.JULY));
        bloqueos.addAll(obtenerBloqueos("data/bloqueosAgosto.txt", Month.AUGUST));
        bloqueos.addAll(obtenerBloqueos("data/bloqueosSeptiembre.txt", Month.SEPTEMBER));
        bloqueos.addAll(obtenerBloqueos("data/bloqueosOctubre.txt", Month.OCTOBER));
        bloqueos.addAll(obtenerBloqueos("data/bloqueosNoviembre.txt", Month.NOVEMBER));
        bloqueos.addAll(obtenerBloqueos("data/bloqueosDiciembre.txt", Month.DECEMBER));
        return bloqueos;
    }
    public static ArrayList<Bloqueo> obtenerBloqueos(String filePath, Month mes) throws IOException {
        ArrayList<Bloqueo> bloqueos = new ArrayList<>();
        Path path = Paths.get(filePath);
        // Base temporal: primer día del mes actual a las 00:00

        for (String line : Files.readAllLines(path)) {
            if (line.isBlank())
                continue;
            String[] parts = line.split(":");
            // Cada rango es "dd'd'HH'h'MM'm'-dd'd'HH'h'MM'm'"
            String[] span = parts[0].split("-");
            LocalDateTime start = parsearFechaHora(span[0], mes);
            LocalDateTime end = parsearFechaHora(span[1],  mes);
            String[] coords = parts[1].split(",");
            ArrayList<Nodo> nodos = new ArrayList<>();
            for (int i = 0; i < coords.length; i += 2) {
                int x = Integer.parseInt(coords[i]);
                int y = Integer.parseInt(coords[i + 1]);
                nodos.add(new Nodo(x, y));
            }
            bloqueos.add(new Bloqueo(nodos, start, end));
        }

        return bloqueos;
    }

    private static LocalDateTime parseOffset(String ym, String offset) {
        int year = Integer.parseInt(ym.substring(0, 4));
        int month = Integer.parseInt(ym.substring(4, 6));
        // offset "dd'd'HH'h'MM'm'"
        String[] ts = offset.split("[dhm]");
        int d = Integer.parseInt(ts[0]);
        int h = Integer.parseInt(ts[1]);
        int m = Integer.parseInt(ts[2]);
        return LocalDateTime.of(year, month, d, h, m);
    }

    /**
     * Carga mantenimientos desde archivo con formato:
     * yyyyMMdd:TTNN
     * Ventana de 24h.
     * 
     * public static ArrayList<Mantenimiento> cargarMantenimientos(String filePath)
     * throws IOException {
     * ArrayList<Mantenimiento> list = new ArrayList<>();
     * Path path = Paths.get(filePath);
     * DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
     * for (String line : Files.readAllLines(path)) {
     * if (line.isBlank())
     * continue;
     * String[] parts = line.split(":");
     * LocalDate date = LocalDate.parse(parts[0], fmt);
     * LocalDateTime start = date.atStartOfDay();
     * LocalDateTime end = start.plusHours(24);
     * String codigo = parts[1];
     * Mantenimiento m = new Mantenimiento(start, end, codigo, "preventivo");
     * list.add(m);
     * }
     * return list;
     * }
     */
    public static void cargarMantenimientos(String archivo, List<Camion> camiones) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String linea;

        while ((linea = br.readLine()) != null) {
            linea = linea.trim();
            if (linea.isEmpty() || linea.startsWith("#")) {
                continue;
            }

            String[] partes = linea.split(":");
            if (partes.length != 2) {
                System.err.println("Línea inválida: " + linea);
                continue;
            }

            String fechaString = partes[0].trim();
            String codigoCamion = partes[1].trim();

            int anho = Integer.parseInt(fechaString.substring(0, 4));
            int mes = Integer.parseInt(fechaString.substring(4, 6));
            int dia = Integer.parseInt(fechaString.substring(6, 8));

            LocalDateTime inicio = LocalDateTime.of(anho, mes, dia, 0, 0);
            LocalDateTime fin = LocalDateTime.of(anho, mes, dia, 23, 59);

            TimeRange rango = new TimeRange(inicio, fin);

            Camion camionEncontrado = camiones.stream()
                    .filter(c -> c.getCodigo().equals(codigoCamion))
                    .findFirst()
                    .orElse(null);

            if (camionEncontrado != null) {
                if (camionEncontrado.getMantenimientos() == null) {
                    camionEncontrado.setMantenimientos(new ArrayList<>());
                }
                camionEncontrado.getMantenimientos().add(rango);
            } else {
                System.err.println("Camión con código " + codigoCamion + " no encontrado.");
            }
        }
        br.close();
    }

    private static LocalDateTime parsearFechaHora(String texto, Month mes) {
        texto = texto.trim();

        String[] partes = texto.split("[dhm]");
        if (partes.length < 3) {
            throw new IllegalArgumentException("Formato de fecha inválido: " + texto);
        }

        int dia = Integer.parseInt(partes[0]);
        int hora = Integer.parseInt(partes[1]);
        int minuto = Integer.parseInt(partes[2]);

        return LocalDateTime.of(2025, mes, dia, hora, minuto, 0, 0);
    }

}
