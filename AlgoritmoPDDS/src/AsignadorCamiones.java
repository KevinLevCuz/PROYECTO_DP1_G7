import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

public class AsignadorCamiones {
    private List<Camion> camiones;
    private List<Planta> plantas;
    private List<Bloqueo> bloqueos;
    private List<Mantenimiento> mantenimientos;
    private LocalDateTime fechaSimulacion;

    public AsignadorCamiones(LocalDateTime fechaSimulacion) {
        this.fechaSimulacion = fechaSimulacion;
    }

    public Solucion asignarPedidos(List<Pedido> pedidosNoEntregados) {
        List<Ruta> rutas = new ArrayList<>();
        List<Pedido> pedidosOrdenados = OrdenarPedidosPorUrgencia(pedidosNoEntregados);
        
        for (Pedido pedido : pedidosOrdenados) {
            Asignacion asignacion = encontrarMejorAsignacion(pedido);
            
            if (asignacion != null) {
                aplicarAsignacion(asignacion);
                rutas.add(asignacion.ruta);
                
                // Registrar mantenimiento rutinario si termina en planta
                if (terminaEnPlanta(asignacion.ruta)) {
                    registrarMantenimientoRutina(asignacion.camion);
                }
            }
        }
        
        return new Solucion(rutas);
    }
    private boolean terminaEnPlanta(Ruta ruta) {
        if (ruta.getNodos().isEmpty()) return false;
        
        Nodo ultimoNodo = ruta.getNodos().get(ruta.getNodos().size()-1);
        return plantas.stream().anyMatch(p -> 
            p.getUbicacion().getPosX() == ultimoNodo.getPosX() && 
            p.getUbicacion().getPosY() == ultimoNodo.getPosY());
    }
    private boolean pasaPorPlanta(List<Nodo> nodos) {
        return nodos.stream().anyMatch(nodo -> 
            plantas.stream().anyMatch(planta ->
                planta.getUbicacion().getPosX() == nodo.getPosX() &&
                planta.getUbicacion().getPosY() == nodo.getPosY()));
    }

    private double calcularTiempoTotalEntrega(Ruta ruta) {
        double tiempoRuta = calcularDistanciaTotal(ruta.getNodos()) * factorConversionTiempo();
    
        if (fechaSimulacion == null || ruta.getFechaPartida() == null) {
            System.out.println("Fecha nula detectada. FechaSimulacion: " + fechaSimulacion + ", FechaPartida: " + ruta.getFechaPartida());
            return Double.MAX_VALUE; // penalización o ignora esta ruta
        }
    
        long minutosEspera = Duration.between(fechaSimulacion, ruta.getFechaPartida()).toMinutes();
        return tiempoRuta + Math.max(0, minutosEspera);
    }
    
    

    private List<Pedido> OrdenarPedidosPorUrgencia(List<Pedido> pedidos) {
        return pedidos.stream()
            .filter(p -> !p.isEntregado())
            .sorted(Comparator.comparingDouble((Pedido p) -> {
                double tiempoRestante = Duration.between(fechaSimulacion, p.getFechaMaximaEntrega()).toMinutes();
                
                double tiempoMinimoEntrega = camiones.stream()
                    .filter(Camion::isDisponible)
                    .mapToDouble(camion -> calcularTiempoEstimadaCamion(camion, p, plantas))
                    .min()
                    .orElse(Double.MAX_VALUE);
                
                return tiempoRestante - tiempoMinimoEntrega;
            })
            .thenComparingDouble(Pedido::getCantidadGlp).reversed())
            .collect(Collectors.toList());
    }

    private double calcularTiempoEstimadaCamion(Camion camion, Pedido pedido, List<Planta> plantas) { //Este es tiempo de manhattan completa sin bloqueos.
        boolean necesitaRecarga = camion.getGlpCargaRest() < pedido.getCantidadGlp();
        
        double distanciaTotal;
        if (necesitaRecarga) {
            Planta plantaCercana = encontrarPlantaCercana(camion.getUbicacion(), plantas);
            distanciaTotal = distanciaEntre(camion.getUbicacion(), plantaCercana.getUbicacion()) 
                          + distanciaEntre(plantaCercana.getUbicacion(), pedido.getUbicacion());
        } else {
            distanciaTotal = distanciaEntre(camion.getUbicacion(), pedido.getUbicacion());
        }
        
        // 3. Convertir distancia a tiempo (suponiendo velocidad constante)
        return distanciaTotal * factorConversionTiempo(); //Este es tiempo sin considerar bloqueos.
    }

    private Planta encontrarPlantaCercana(Nodo ubicacion, List<Planta> plantas) {
        return plantas.stream()
            .min(Comparator.comparingDouble(p -> 
                distanciaEntre(ubicacion, p.getUbicacion())))
            .orElseThrow(() -> new RuntimeException("No hay plantas disponibles"));
    }

    private double distanciaEntre(Nodo a, Nodo b) {
        // Distancia Manhattan (puedes cambiarlo por otra métrica)
        return Math.abs(a.getPosX() - b.getPosX()) + Math.abs(a.getPosY() - b.getPosY());
    }
    private double calcularDistanciaTotal(List<Nodo> nodos) {
        if (nodos.size() < 2) return 0;
        
        double distancia = 0;
        for (int i = 1; i < nodos.size(); i++) {
            distancia += distanciaEntre(nodos.get(i-1), nodos.get(i));
        }
        return distancia;
    }
    private double factorConversionTiempo() {
        return 1.2; 
    }

    private Asignacion encontrarMejorAsignacion(Pedido pedido) {
        Asignacion mejorAsignacion = null;
        double mejorTiempo = Double.MAX_VALUE;
        
        for (Camion camion : camiones) {
            Asignacion asignacion = evaluarAsignacion(camion, pedido);
            
            if (asignacion != null && asignacion.tiempoEntrega < mejorTiempo) {
                mejorAsignacion = asignacion;
                mejorTiempo = asignacion.tiempoEntrega;
            }
        }
        
        return mejorAsignacion;
    }

    private void aplicarAsignacion(Asignacion asignacion) {
        Camion camion = asignacion.camion;
        Pedido pedido = asignacion.pedido;

        camion.setGlpCargaRest(camion.getGlpCargaRest() - pedido.getCantidadGlp());
        
        //double distanciaTotal = calcularDistanciaTotal(asignacion.ruta.getNodos());
        //double consumo = camion.calcularConsumo(distanciaTotal);
        //camion.setGlpTanqueRest(camion.getGlpTanqueRest() - consumo);
        
        pedido.setRuta(asignacion.ruta);
        pedido.setEntregado(false);
        
        // 3. Si la ruta pasa por una planta, recargar tanque completamente
        if (pasaPorPlanta(asignacion.ruta.getNodos())) {
            camion.setGlpTanqueRest(camion.getGlpTanque());
        }
        
        // 4. Actualizar ubicación del camión (al final de la ruta)
        //if (!asignacion.ruta.getNodos().isEmpty()) {
          //  Nodo nuevaUbicacion = asignacion.ruta.getNodos().get(asignacion.ruta.getNodos().size()-1);
          // camion.setUbicacion(nuevaUbicacion);
        // }
    }
    
    private Asignacion evaluarAsignacion(Camion camion, Pedido pedido) {
        // 1. Verificar disponibilidad
        LocalDateTime fechaPartida = calcularFechaPartidaOptima(pedido, camion);
        if (!estaDisponible(camion, fechaPartida)) {
            return null;
        }
        // 2. Calcular ruta óptima
        Ruta ruta = calcularRutaOptimaCompleta(camion, pedido, fechaPartida);
        if (ruta == null) {
            return null;
        }
        
        // 3. Verificar combustible
        if (!verificarCombustible(camion, ruta)) {
            return null;
        }

        // 4. Calcular tiempo de entrega
        double tiempoEntrega = calcularTiempoTotalEntrega(ruta);
        
        return new Asignacion(camion, pedido, ruta, tiempoEntrega);
    }
    
    private LocalDateTime calcularFechaPartidaOptima(Pedido pedido, Camion camion) {
        double tiempoRuta = calcularTiempoEstimadaCamion(camion, pedido, plantas);
        LocalDateTime fechaMinimaPartida = pedido.getFechaRegistro().plusHours(4).minusMinutes((long)tiempoRuta);
        LocalDateTime fechaMaximaPartida = pedido.getFechaMaximaEntrega().minusMinutes((long)tiempoRuta);
        // Elegir la fecha más temprana posible que cumpla ambas condiciones
        if (fechaSimulacion.isAfter(fechaMinimaPartida)) {
            return fechaSimulacion;
        }
        return fechaMinimaPartida.isBefore(fechaMaximaPartida) ? fechaMinimaPartida : null;
    }
    
    private boolean estaDisponible(Camion camion, LocalDateTime fechaRequerida) {
        return mantenimientos.stream().noneMatch(m -> 
            m.getCodigoCamion().equals(camion.getCodigo()) &&
            fechaRequerida.isAfter(m.getInicio()) &&
            fechaRequerida.isBefore(m.getFin()));
    }
    
    private Ruta calcularRutaOptimaCompleta(Camion camion, Pedido pedido, LocalDateTime fechaPartida) {
        boolean necesitaRecarga = camion.getGlpCargaRest() < pedido.getCantidadGlp();
        List<Nodo> rutaCalculada = new ArrayList<>();
        
        if (necesitaRecarga) {
            Planta planta = encontrarPlantaCercana(camion.getUbicacion(), plantas);
            List<Nodo> rutaAPlanta = calcularRuta(camion.getUbicacion(), planta.getUbicacion());
            List<Nodo> rutaAPedido = calcularRuta(planta.getUbicacion(), pedido.getUbicacion());
            
            rutaCalculada.addAll(rutaAPlanta);
            rutaCalculada.addAll(rutaAPedido);
        } else {
            rutaCalculada = calcularRuta(camion.getUbicacion(), pedido.getUbicacion());
        }
        
        // Verificar si pasa por una planta (recarga automática)
        if (pasaPorPlanta(rutaCalculada)) {
            camion.setGlpTanqueRest(camion.getGlpTanque());
        }
        return new Ruta(camion, rutaCalculada, fechaPartida);
    }
    
    private boolean verificarCombustible(Camion camion, Ruta ruta) {
        double distanciaTotal = calcularDistanciaTotal(ruta.getNodos());
        return camion.tieneCombustibleSuficiente(distanciaTotal);
    }
    
    private void registrarMantenimientoRutina(Camion camion) {
        LocalDateTime ahora = fechaSimulacion;
        Mantenimiento rutina = new Mantenimiento(
            ahora,
            ahora.plusMinutes(15),
            camion.getCodigo(),
            "RUTINA"
        );
        mantenimientos.add(rutina);
    }
   
    private boolean estaBloqueado(int x, int y) {
        LocalDateTime ahora = fechaSimulacion;
        return bloqueos.stream().anyMatch(b ->
            ahora.isAfter(b.getFechaHoraInicio()) &&
            ahora.isBefore(b.getFechaHoraFin()) &&
            b.getNodos().stream().anyMatch(n ->
                n.getPosX() == x && n.getPosY() == y));
    }

    private List<Nodo> calcularRuta(Nodo inicio, Nodo fin) {
        if (!esNodoValido(inicio) || !esNodoValido(fin)) {
            throw new IllegalArgumentException("Los nodos deben tener coordenadas validas");
        }
        // Implementación del algoritmo A* con manejo de bloqueos
        PriorityQueue<NodoAEstrella> abiertos = new PriorityQueue<>(Comparator.comparingDouble(NodoAEstrella::getCostoTotal));
        Set<Nodo> cerrados = new HashSet<>();
        Map<Nodo, Nodo> padres = new HashMap<>();
        Map<Nodo, Double> costos = new HashMap<>();

        // Inicialización
        NodoAEstrella nodoInicial = new NodoAEstrella(inicio, 0, distanciaHeuristica(inicio, fin));
        abiertos.add(nodoInicial);
        costos.put(inicio, 0.0);

        while (!abiertos.isEmpty()) {
            NodoAEstrella actual = abiertos.poll();
            Nodo nodoActual = actual.getNodo();

            if (nodoActual.equals(fin)) {
                return reconstruirRuta(padres, fin);
            }

            cerrados.add(nodoActual);

            for (Nodo vecino : obtenerVecinosValidos(nodoActual, fin)) {
                if (cerrados.contains(vecino)) {
                    continue;
                }

                double nuevoCosto = costos.get(nodoActual) + 1; 

                if (!costos.containsKey(vecino) || nuevoCosto < costos.get(vecino)) {
                    costos.put(vecino, nuevoCosto);
                    double costoTotal = nuevoCosto + distanciaHeuristica(vecino, fin);
                    abiertos.add(new NodoAEstrella(vecino, nuevoCosto, costoTotal));
                    padres.put(vecino, nodoActual);
                }
            }
        }

    //En teoría nunca debería llegar aquí porque siempre hay solución
    return Collections.emptyList();
}
private boolean esNodoValido(Nodo nodo) {
    return nodo.getPosX() >= 0 && nodo.getPosY() >= 0 && nodo.getPosX()<=70 && nodo.getPosY()<=50;
}
private List<Nodo> reconstruirRuta(Map<Nodo, Nodo> padres, Nodo fin) {
    List<Nodo> ruta = new ArrayList<>();
    Nodo actual = fin;

    while (actual != null) {
        ruta.add(actual);
        actual = padres.get(actual);
    }

    Collections.reverse(ruta);
    return ruta;
}

private double distanciaHeuristica(Nodo a, Nodo b) {
    // Distancia Manhattan (adecuada para movimientos en 4 direcciones)
    return Math.abs(a.getPosX() - b.getPosX()) + Math.abs(a.getPosY() - b.getPosY());
}

private List<Nodo> obtenerVecinosValidos(Nodo actual, Nodo fin) {
    List<Nodo> vecinos = new ArrayList<>();
    int[][] direcciones = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // Arriba, derecha, abajo, izquierda

    for (int[] dir : direcciones) {
        int x = actual.getPosX() + dir[0];
        int y = actual.getPosY() + dir[1];
        
        // Verificar si el vecino es válido (no está bloqueado)
        if (x >= 0 && y >= 0 && x<=70 && y<=50 && !estaBloqueado(x, y)) {
            vecinos.add(new Nodo(x, y));
        }
    }

    // Si estamos muy cerca del destino y hay un atajo directo
    if (distanciaHeuristica(actual, fin) == 1) {
        if (esNodoValido(fin) && !estaBloqueado(fin.getPosX(), fin.getPosY())) {
            vecinos.add(fin);
        }
    }

    return vecinos;
}

// Clase auxiliar para el algoritmo A*
private static class NodoAEstrella {
    private Nodo nodo;
    private double costoDesdeInicio;
    private double costoTotalEstimado;

    public NodoAEstrella(Nodo nodo, double costoDesdeInicio, double costoTotalEstimado) {
        this.nodo = nodo;
        this.costoDesdeInicio = costoDesdeInicio;
        this.costoTotalEstimado = costoTotalEstimado;
    }

    public Nodo getNodo() { return nodo; }
    public double getCostoDesdeInicio() { return costoDesdeInicio; }
    public double getCostoTotal() { return costoTotalEstimado; }
}


    private class Asignacion {
        Camion camion;
        Pedido pedido;
        Ruta ruta;
        double tiempoEntrega;
        
        public Asignacion(Camion camion, Pedido pedido, Ruta ruta, double tiempoEntrega) {
            this.camion = camion;
            this.pedido = pedido;
            this.ruta = ruta;
            this.tiempoEntrega = tiempoEntrega;
        }
    }



    public List<Camion> getCamiones() {
        return camiones;
    }

    public void setCamiones(List<Camion> camiones) {
        this.camiones = camiones;
    }

    public List<Planta> getPlantas() {
        return plantas;
    }

    public void setPlantas(List<Planta> plantas) {
        this.plantas = plantas;
    }

    public List<Bloqueo> getBloqueos() {
        return bloqueos;
    }

    public void setBloqueos(List<Bloqueo> bloqueos) {
        this.bloqueos = bloqueos;
    }

    public List<Mantenimiento> getMantenimientos() {
        return mantenimientos;
    }

    public void setMantenimientos(List<Mantenimiento> mantenimientos) {
        this.mantenimientos = mantenimientos;
    }
}