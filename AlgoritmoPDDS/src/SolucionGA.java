import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SolucionGA {
    private static final Random rand = new Random();
    private static final double PROBABILIDAD_CRUCE = 0.8;
    private static final double PROBABILIDAD_MUTACION = 0.1;
    private static final int TAMANIO_POBLACION = 30;
    private static final int NUM_GENERACIONES = 5;
    private static final int TAMANIO_TORNEO = 3;
    
    private List<Pedido> pedidos;
    private List<Camion> camiones;
    private List<Planta> plantas;
    private LocalDateTime fechaSimulada;

    public SolucionGA(List<Pedido> pedidos, List<Camion> camiones, List<Planta> plantas, LocalDateTime fechaSimulada) {
        this.pedidos = pedidos;
        this.camiones = camiones;
        this.plantas = plantas;
        this.fechaSimulada = fechaSimulada;
    }
    
    public List<Asignacion> ejecutarAlgoritmoGenetico(List<Pedido> pedidosTodos) {
        // 1. Generar población inicial
        List<Solucion> poblacion = generarPoblacionInicial();
        
        // 2. Evaluar población inicial
        evaluarPoblacion(poblacion,pedidosTodos);
        
        // 3. Evolución
        for (int generacion = 0; generacion < NUM_GENERACIONES; generacion++) {
            List<Solucion> nuevaPoblacion = new ArrayList<>();
            
            // Elitismo: mantener la mejor solución
            nuevaPoblacion.add(Collections.max(poblacion, Comparator.comparingDouble(Solucion::getFitness)));
            
            while (nuevaPoblacion.size() < TAMANIO_POBLACION) {
                // Selección
                Solucion padre1 = seleccionarPorTorneo(poblacion);
                Solucion padre2 = seleccionarPorTorneo(poblacion);
                
                // Cruce
                Solucion hijo = cruzar(padre1, padre2);
                
                // Mutación
                if (rand.nextDouble() < PROBABILIDAD_MUTACION) {
                    mutar(hijo);
                }
                
                nuevaPoblacion.add(hijo);
            }
            
            poblacion = nuevaPoblacion;
            evaluarPoblacion(poblacion,pedidosTodos);
        }
        
        // Devolver la mejor solución
        return Collections.max(poblacion, Comparator.comparingDouble(Solucion::getFitness)).getSolucion();
    }
    
    private List<Solucion> generarPoblacionInicial() {
        List<Solucion> poblacion = new ArrayList<>();
        
        // Solución inicial (heurística)
        Main main = new Main();
        List<Asignacion> asignacionesIniciales = main.generarSolucionInicial(pedidos, camiones, plantas, fechaSimulada);
        poblacion.add(new Solucion(asignacionesIniciales));
        
        // Soluciones aleatorias para completar población
        while (poblacion.size() < TAMANIO_POBLACION) {
            poblacion.add(generarSolucionAleatoria());
        }
        
        return poblacion;
    }
    
    private Solucion generarSolucionAleatoria() {
        List<Asignacion> asignaciones = new ArrayList<>();
        List<Camion> camionesDisponibles = new ArrayList<>(camiones);
        List<Pedido> pedidosPendientes = new ArrayList<>(pedidos);
        
        Collections.shuffle(pedidosPendientes);
        Collections.shuffle(camionesDisponibles);
        
        Main main = new Main();
        for (Pedido pedido : pedidosPendientes) {
            for (Camion camion : camionesDisponibles) {
                if (camion.isDisponible(fechaSimulada)) {
                    List<SubRuta> subRutas = main.intentarAsignarPedidoSimple(
                        camion, pedido, plantas, pedidos);
                    
                    if (subRutas != null) {
                        Asignacion asignacion = new Asignacion(
                            camion, subRutas, subRutas.getFirst().getFechaPartida());
                        asignaciones.add(asignacion);
                        break;
                    }
                }
            }
        }
        
        return new Solucion(asignaciones);
    }
    
    private void evaluarPoblacion(List<Solucion> poblacion,List<Pedido> pedidosTodos) {
        for (Solucion solucion : poblacion) {
            solucion.setFitness(calcularFitness(solucion.getSolucion(),pedidosTodos));
        }
    }
    /* 
    private double calcularFitness(List<Asignacion> asignaciones) {
        // 1. Porcentaje de pedidos atendidos (más importante)
        long pedidosAtendidos = asignaciones.stream()
            .flatMap(a -> a.getSubRutas().stream())
            .filter(sr -> sr.getPedido() != null)
            .count();
        double porcentajeAtendidos = (double) pedidosAtendidos / pedidos.size();
        
        // 2. Tiempo promedio de entrega (cuanto menor, mejor)
        double tiempoTotal = 0;
        int contadorTiempo = 0;
        for (Asignacion a : asignaciones) {
            for (SubRuta sr : a.getSubRutas()) {
                if (sr.getPedido() != null) {
                    double tiempo = Duration.between(
                        sr.getPedido().getFechaRegistro(), 
                        sr.getFechaLlegada()).toMinutes();
                    tiempoTotal += tiempo;
                    contadorTiempo++;
                }
            }
        }
        double tiempoPromedio = contadorTiempo > 0 ? tiempoTotal / contadorTiempo : 0;
        
        // 3. Distancia total recorrida (cuanto menor, mejor)
        double distanciaTotal = asignaciones.stream()
            .mapToDouble(a -> a.getSubRutas().stream()
                .mapToDouble(sr -> sr.getTrayectoria().size() - 1)
                .sum())
            .sum();
        
        // 4. Penalización por pedidos no atendidos
        double penalizacionNoAtendidos = (pedidos.size() - pedidosAtendidos) * 1000;
        
        // Fórmula de fitness (puedes ajustar los pesos)
        return (porcentajeAtendidos * 1000) - 
               (tiempoPromedio * 0.1) - 
               (distanciaTotal * 0.01) - 
               penalizacionNoAtendidos;
    }*/
    public static double calcularFitness(List<Asignacion> asignaciones,List<Pedido> pedidosTodos) {
        // Constantes de penalización (deberían definirse como constantes de clase)
        final double EARLY_PENALTY = 10; // Penalización por entrega temprana
        final double DEADLINE_PENALTY = 10000; // Penalización por entrega tardía
        final double MAINT_PENALTY = 10000; // Penalización por conflicto con mantenimiento
        final double BLOCK_PENALTY = 10000; // Penalización por bloqueos
        final double NO_ASSIGN_PENALTY = 3000; // Penalización por pedido no asignado
        
        double totalCost = 0.0;
        
        // 1. Calcular costos de las asignaciones existentes
        for (Asignacion asignacion : asignaciones) {
            Camion camion = asignacion.getCamion();
            
            for (SubRuta subRuta : asignacion.getSubRutas()) {
                LocalDateTime t = subRuta.getFechaPartida();
                Pedido p = subRuta.getPedido();
                
                // Penalización por entrega temprana (antes de 4 horas)
                if (p != null) {
                    LocalDateTime earliest = p.getFechaRegistro().plusHours(4);
                    if (t.isBefore(earliest)) {
                        totalCost += EARLY_PENALTY;
                    }
                }
                
                // Penalización por mantenimiento
                for (TimeRange mantenimiento : camion.getMantenimientos()) {
                    if (!t.isBefore(mantenimiento.getStart()) && 
                        t.isBefore(mantenimiento.getEnd())) {
                        totalCost += MAINT_PENALTY;
                    }
                }
                
                // Costo por distancia, consumo y bloqueos
                double glp = camion.getGlpTanqueRest();
                LocalDateTime tiempoActual = t;
                
                for (int k = 1; k < subRuta.getTrayectoria().size(); k++) {
                    Nodo a = subRuta.getTrayectoria().get(k - 1);
                    Nodo b = subRuta.getTrayectoria().get(k);
                    double d = 1; // Cada paso en la trayectoria representa 1 unidad de distancia
                    
                    // Costo por distancia y consumo
                    double consumo = camion.calcularConsumo(d);
                    totalCost += d + consumo;
                    glp -= consumo;
                    
                    // Actualizar tiempo de llegada al nodo
                    tiempoActual = tiempoActual.plusSeconds(72); // 72 segundos por unidad de distancia
                    
                    // Penalización por bloqueos
                    if (b.isBlockedAt(tiempoActual)) {
                        totalCost += BLOCK_PENALTY;
                    }
                }
                
                // Penalización por entrega tardía
                if (p != null && tiempoActual.isAfter(p.getFechaMaximaEntrega())) {
                    totalCost += DEADLINE_PENALTY;
                }
            }
        }
        
        // 2. Penalización por pedidos no asignados
        List<Pedido> pedidosAsignados = asignaciones.stream()
            .flatMap(a -> a.getSubRutas().stream())
            .filter(sr -> sr.getPedido() != null)
            .map(SubRuta::getPedido)
            .collect(Collectors.toList());
        
        long pedidosNoAsignados = pedidosTodos.stream()
            .filter(p -> !pedidosAsignados.contains(p))
            .count();
        
        totalCost += pedidosNoAsignados * NO_ASSIGN_PENALTY;
        
        // Convertir el costo a fitness (a mayor costo, menor fitness)
        return 1.0 / (1.0 + totalCost);
    }
    
    private Solucion seleccionarPorTorneo(List<Solucion> poblacion) {
        List<Solucion> torneo = new ArrayList<>();
        for (int i = 0; i < TAMANIO_TORNEO; i++) {
            torneo.add(poblacion.get(rand.nextInt(poblacion.size())));
        }
        return Collections.max(torneo, Comparator.comparingDouble(Solucion::getFitness));
    }
    
    private Solucion cruzar(Solucion padre1, Solucion padre2) {
        if (rand.nextDouble() > PROBABILIDAD_CRUCE) {
            return rand.nextBoolean() ? padre1 : padre2;
        }
        
        // Cruce basado en pedidos asignados
        List<Asignacion> hijoAsignaciones = new ArrayList<>();
        
        // Tomar algunos pedidos asignados del padre1
        List<Pedido> pedidosPadre1 = padre1.getSolucion().stream()
            .flatMap(a -> a.getSubRutas().stream())
            .filter(sr -> sr.getPedido() != null)
            .map(SubRuta::getPedido)
            .collect(Collectors.toList());
        
        // Tomar algunos pedidos asignados del padre2 que no estén en padre1
        List<Pedido> pedidosPadre2 = padre2.getSolucion().stream()
            .flatMap(a -> a.getSubRutas().stream())
            .filter(sr -> sr.getPedido() != null && !pedidosPadre1.contains(sr.getPedido()))
            .map(SubRuta::getPedido)
            .collect(Collectors.toList());
        
        // Combinar pedidos y asignar con camiones disponibles
        List<Pedido> pedidosCombinados = new ArrayList<>();
        pedidosCombinados.addAll(pedidosPadre1);
        pedidosCombinados.addAll(pedidosPadre2);
        
        Main main = new Main();
        List<Camion> camionesDisponibles = new ArrayList<>(camiones);
        Collections.shuffle(camionesDisponibles);
        
        for (Pedido pedido : pedidosCombinados) {
            for (Camion camion : camionesDisponibles) {
                if (camion.isDisponible(fechaSimulada)) {
                    List<SubRuta> subRutas = main.intentarAsignarPedidoSimple(
                        camion, pedido, plantas, pedidos);
                    
                    if (subRutas != null) {
                        Asignacion asignacion = new Asignacion(
                            camion, subRutas, subRutas.getFirst().getFechaPartida());
                        hijoAsignaciones.add(asignacion);
                        break;
                    }
                }
            }
        }
        
        return new Solucion(hijoAsignaciones);
    }
    
    private void mutar(Solucion solucion) {
        // Mutación: cambiar aleatoriamente algunas asignaciones
        List<Asignacion> asignaciones = solucion.getSolucion();
        
        // Tipo de mutación aleatoria
        int tipoMutacion = rand.nextInt(3);
        
        switch (tipoMutacion) {
            case 0: // Eliminar una asignación aleatoria
                if (!asignaciones.isEmpty()) {
                    asignaciones.remove(rand.nextInt(asignaciones.size()));
                }
                break;
                
            case 1: // Cambiar camión de una asignación
                if (!asignaciones.isEmpty()) {
                    Asignacion aMutada = asignaciones.get(rand.nextInt(asignaciones.size()));
                    List<Camion> camionesDisponibles = camiones.stream()
                        .filter(c -> c.isDisponible(fechaSimulada) && !c.equals(aMutada.getCamion()))
                        .collect(Collectors.toList());
                    
                    if (!camionesDisponibles.isEmpty()) {
                        Camion nuevoCamion = camionesDisponibles.get(rand.nextInt(camionesDisponibles.size()));
                        Main main = new Main();
                        for (SubRuta sr : aMutada.getSubRutas()) {
                            if (sr.getPedido() != null) {
                                List<SubRuta> nuevasSubRutas = main.intentarAsignarPedidoSimple(
                                    nuevoCamion, sr.getPedido(), plantas, pedidos);
                                
                                if (nuevasSubRutas != null) {
                                    asignaciones.remove(aMutada);
                                    asignaciones.add(new Asignacion(
                                        nuevoCamion, nuevasSubRutas, nuevasSubRutas.getFirst().getFechaPartida()));
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
                
            case 2: // Agregar un pedido no asignado
                List<Pedido> pedidosAsignados = asignaciones.stream()
                    .flatMap(a -> a.getSubRutas().stream())
                    .filter(sr -> sr.getPedido() != null)
                    .map(SubRuta::getPedido)
                    .collect(Collectors.toList());
                
                List<Pedido> pedidosNoAsignados = pedidos.stream()
                    .filter(p -> !pedidosAsignados.contains(p))
                    .collect(Collectors.toList());
                
                if (!pedidosNoAsignados.isEmpty()) {
                    Pedido pedido = pedidosNoAsignados.get(rand.nextInt(pedidosNoAsignados.size()));
                    Main main = new Main();
                    
                    for (Camion camion : camiones) {
                        if (camion.isDisponible(fechaSimulada)) {
                            List<SubRuta> subRutas = main.intentarAsignarPedidoSimple(
                                camion, pedido, plantas, pedidos);
                            
                            if (subRutas != null) {
                                asignaciones.add(new Asignacion(
                                    camion, subRutas, subRutas.getFirst().getFechaPartida()));
                                break;
                            }
                        }
                    }
                }
                break;
        }
    }
}