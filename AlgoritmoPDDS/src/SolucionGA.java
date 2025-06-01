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
    private static final int TAMANIO_POBLACION = 7;
    private static final int NUM_GENERACIONES = 3;
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
    
    public List<Asignacion> ejecutarAlgoritmoGenetico(List<Pedido> pedidosParaAsignar) {

        List<Solucion> poblacion = generarPoblacionInicial();

        evaluarPoblacion(poblacion,pedidosParaAsignar);
        
        for (int generacion = 0; generacion < NUM_GENERACIONES; generacion++) {
            List<Solucion> nuevaPoblacion = new ArrayList<>();
            
            nuevaPoblacion.add(Collections.max(poblacion, Comparator.comparingDouble(Solucion::getFitness)));
            
            while (nuevaPoblacion.size() < TAMANIO_POBLACION) {
                // Generamos la selección, en este caso por torneo.
                Solucion padre1 = seleccionarPorTorneo(poblacion);
                Solucion padre2 = seleccionarPorTorneo(poblacion);
                
                // Hacemos el cruce
                Solucion hijo = cruzar(padre1, padre2);
                
                // Hacemos la mutación
                if (rand.nextDouble() < PROBABILIDAD_MUTACION) {
                    mutar(hijo);
                }
                
                nuevaPoblacion.add(hijo);
            }
            
            poblacion = nuevaPoblacion;
            evaluarPoblacion(poblacion,pedidosParaAsignar);
        }
        
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
        
        // Reseteamos a los camiones con su asignación simulada a false
        for (Camion camion : camionesDisponibles) {
            camion.setAsignacionSimulada(false);
            camion.resetSimulacion();
        }

        Collections.shuffle(pedidosPendientes);
        Collections.shuffle(camionesDisponibles);

        
        Main main = new Main();
        for (Pedido pedido : pedidosPendientes) {
            for (Camion camion : camionesDisponibles) {
                if (camion.isDisponible(fechaSimulada)) {
                    
                    if(camion.isAsignacionSimulada()){
                        continue;   
                    }
                    List<SubRuta> subRutas = main.intentarAsignarPedidoSimple(camion, pedido, plantas, pedidos, fechaSimulada);
                    
                    if (subRutas != null) {
                        camion.setAsignacionSimulada(true);
                        
                        Asignacion asignacion = new Asignacion(
                            camion, subRutas, subRutas.getFirst().getFechaPartida());

                        main.confirmarAsignacionSimulada(asignacion, pedido, plantas);
                        asignaciones.add(asignacion);
                        break;
                    }
                }
            }
        } 
        
        return new Solucion(asignaciones);
    }
    
    private void evaluarPoblacion(List<Solucion> poblacion,List<Pedido> pedidosParaAsignar) {
        for (Solucion solucion : poblacion) {
            solucion.setFitness(calcularFitness(solucion.getSolucion(),pedidosParaAsignar));
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
    public static double calcularFitness(List<Asignacion> asignaciones, List<Pedido> pedidosPaAsignar) {
        // Constantes de penalización (deberían definirse como constantes de clase)
        final double EARLY_PENALTY = 10; // Penalización por entrega temprana
        final double DEADLINE_PENALTY = 100000; // Penalización por entrega tardía
        final double MAINT_PENALTY = 100000; // Penalización por conflicto con mantenimiento
        final double BLOCK_PENALTY = 100000; // Penalización por bloqueos
        final double NO_ASSIGN_PENALTY = 10000; // Penalización por pedido no asignado
        
        double totalCost = 0.0;
        
        // 1. Calcular costos de las asignaciones existentes
        for (Asignacion asignacion : asignaciones) {
            Camion camion = asignacion.getCamion();
            double distancia = Utilidades.calcularDistanciaTotal(asignacion);
            double consumo = camion.calcularConsumo(distancia);
            totalCost += distancia+consumo;
            for (SubRuta subRuta : asignacion.getSubRutas()) {
                LocalDateTime t = subRuta.getFechaPartida();
                Pedido p = subRuta.getPedido();
                
                // Penalización por entrega temprana (antes de 4 horas)
                if (p != null) {
                    LocalDateTime earliest = p.getFechaRegistro().plusHours(4);
                    if (subRuta.getFechaLlegada().isBefore(earliest)) {
                        System.out.println("Esta ingresando con: "+subRuta.getFechaLlegada()+" y el earlist es: "+earliest);
                        System.out.println("Entro a penalizacion por entrega temprana.");
                        totalCost += EARLY_PENALTY;
                    }
                }
                
                // Penalización por mantenimiento
                for (TimeRange mantenimiento : camion.getMantenimientos()) {
                    if (!t.isBefore(mantenimiento.getStart()) && 
                        t.isBefore(mantenimiento.getEnd())) {
                        System.out.println("Ingreso a MAINT_PENALTY");
                        totalCost += MAINT_PENALTY;
                    }
                }
                
                // Costo por distancia, consumo y bloqueos
                double glp = camion.getGlpTanqueRest();
                LocalDateTime tiempoActual = t;
                // Penalización por entrega tardía
                if (p != null && subRuta.getFechaLlegada().isAfter(p.getFechaMaximaEntrega())) {
                    System.out.println("Ingreso a entrega tardia.");
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
        
        long pedidosNoAsignados = asignaciones.stream()
            .flatMap(a -> a.getSubRutas().stream())
            .filter(sr -> sr.getPedido() == null).count();
        
        return 1.0 / (1.0 + totalCost);
    }
    
    private Solucion seleccionarPorTorneo(List<Solucion> poblacion) {
        List<Solucion> torneo = new ArrayList<>();
        torneo.add(poblacion.get(0));
        for (int i = 0; i < TAMANIO_TORNEO-1; i++) {
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
                        camion, pedido, plantas, pedidos, fechaSimulada);
                    
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
                                    nuevoCamion, sr.getPedido(), plantas, pedidos, fechaSimulada);
                                
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
                                camion, pedido, plantas, pedidos, fechaSimulada);
                            
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