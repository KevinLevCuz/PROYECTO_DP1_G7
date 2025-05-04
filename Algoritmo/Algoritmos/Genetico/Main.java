package Algoritmos.Genetico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import Algoritmos.SA.Mantenimiento;

import java.awt.Point;


public class Main {
    static Random rand = new Random();
    static int TAMANO_POBLACION = 100;
    static int GENERACIONES = 200;
    static double PROB_MUTACION = 0.1;
    static double PROB_CRUCE = 0.8;
    static int TORNEOS_K = 3; 

    public static void main(String[] args) throws IOException {        
        List<Pedido> pedidos = cargarPedidos("data\\pedidos.txt");
        List<Camion> camiones = cargarCamiones("data\\camiones.txt");
        List<Bloqueo> bloqueos = cargarBloqueos("data\\bloqueos.txt");
        
        List<Mantenimiento> mantenimiento = cargaMantenimientos("data\\mantenimiento.txt");

        LocalDateTime fechaHora;
        // AQUI OBTENEMOS LA FECHA SIMULADA.
        if (args.length > 0) {
            // Suponemos que la fecha viene como "yyyy-MM-dd HH:mm"
            String fechaString = args[0];   
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            fechaHora = LocalDateTime.parse(fechaString, formatter);

            System.out.println("Fecha y hora simulada: " + fechaHora);
        } else {
            fechaHora = LocalDateTime.now();
            System.out.println("No se pasó fecha simulada, usando fecha y hora actual: " + fechaHora);
        }


        //Realizar un metodo que actualice los estados de los camiones.
        actualizarEstadoCamiones(fechaHora, camiones, mantenimiento);

        long camionesDisponibles = camiones.stream().filter(Camion::getDisponible).count();
        if (camionesDisponibles == 0) {
            System.err.println("Error: No hay camiones disponibles para la fecha " + fechaHora);
            return;
        }

        // 1. Inicializar población
        List<Solucion> poblacion = inicializarPoblacion(pedidos, camiones);
        
        // 2. Evolución
        for (int generacion = 0; generacion < GENERACIONES; generacion++) {
            double fitnessInicial;
            // Evaluar fitness
            fitnessInicial = evaluarPoblacion(poblacion, bloqueos, fechaHora);
            
            // Seleccionar padres
            List<Solucion> padres = seleccionarPadres(poblacion, bloqueos, fechaHora);
            
            // Cruzar
            List<Solucion> descendientes = cruzar(padres);
            
            // Mutar
            mutar(descendientes, pedidos, camiones);
            
            // Reemplazo generacional
            poblacion = descendientes;
            
            if (generacion % 10 == 0) {
                try {
                    // Encontrar la mejor solución válida
                    Optional<Solucion> mejorSolucion = poblacion.stream()
                        .filter(s -> esSolucionValida(s, camiones))
                        .min(Comparator.comparingDouble(s -> s.costoTotal(bloqueos, fechaHora)));
                    
                    if (mejorSolucion.isPresent()) {
                        System.out.println("Generación " + generacion + " - Mejor costo: " + 
                                mejorSolucion.get().costoTotal(bloqueos, fechaHora));
                    } else {
                        System.out.println("Generación " + generacion + " - No hay soluciones válidas");
                    }
                } catch (Exception e) {
                    System.out.println("Generación " + generacion + " - Error al evaluar soluciones");
                }
            }
        }
        // 3. Obtener mejor solución
        Double fitnessFinal;
        fitnessFinal = evaluarPoblacion(poblacion, bloqueos, fechaHora);
        Solucion mejorSolucion = Collections.min(poblacion, Comparator.comparingDouble(s -> s.costoTotal(bloqueos, fechaHora)));
        
        System.out.println("\n--- MEJOR SOLUCIÓN ENCONTRADA ---");
        System.out.println("Costo total (consumo combustible): " + mejorSolucion.costoTotal(bloqueos, fechaHora));
        mejorSolucion.imprimirRutasDetalladas(camiones,bloqueos); 
    }

    static boolean esSolucionValida(Solucion solucion, List<Camion> camiones) {
        for (Camion camion : camiones) {
            List<Pedido> pedidos = solucion.rutas.get(camion);
            int cargaTotal = pedidos.stream().mapToInt(Pedido::getCantidad).sum();
            if (cargaTotal > camion.getCapacidadEfectiva()) {
                return false;
            }
        }
        return true;
    }
    
    static List<Solucion> inicializarPoblacion(List<Pedido> pedidos, List<Camion> camiones) {
        List<Solucion> poblacion = new ArrayList<>();
        
        for (int i = 0; i < TAMANO_POBLACION; i++) {
            Solucion sol = new Solucion();
            
            // Mezclar pedidos para asignación aleatoria
            List<Pedido> pedidosAleatorios = new ArrayList<>(pedidos);
            Collections.shuffle(pedidosAleatorios);
            
            // Asignar pedidos a camiones respetando capacidades
            for (Camion camion : camiones) {
                if(camion.getDisponible()){
                    sol.rutas.put(camion, new ArrayList<>());
                }
            }

            // Obtener lista de camiones disponibles
            List<Camion> camionesDisponibles = camiones.stream()
                .filter(Camion::getDisponible)
                .collect(Collectors.toList());
                
            if (camionesDisponibles.isEmpty()) {
                throw new RuntimeException("No hay camiones disponibles para asignar pedidos");
            }
            
            for (Pedido pedido : pedidosAleatorios) {
                boolean asignado = false;
                // Intentar asignar a un camión aleatorio con capacidad suficiente
                List<Camion> camionesAleatorios = new ArrayList<>(camionesDisponibles);
                Collections.shuffle(camionesAleatorios);
                
                for (Camion camion : camionesAleatorios) {
                    if (puedeAsignarPedido(camion, sol.rutas.get(camion), pedido)) {
                        sol.rutas.get(camion).add(pedido);
                        asignado = true;
                        break;
                    }
                }
                
                
                if (!asignado) {
                    Camion mejorCamion = null;
                    int espacioDisponible = -1;
                    
                    for (Camion camion : camionesDisponibles) {
                        int cargaActual = sol.rutas.get(camion).stream().mapToInt(Pedido::getCantidad).sum();
                        int espacio = camion.getCapacidadEfectiva() - cargaActual;
                        if (espacio > espacioDisponible && espacio >= pedido.getCantidad()) {
                            espacioDisponible = espacio;
                            mejorCamion = camion;
                        }
                    }
                    
                    if (mejorCamion != null) {
                        sol.rutas.get(mejorCamion).add(pedido);
                    } else {
                        System.err.println("No hay camión disponible con capacidad suficiente para el pedido: " + pedido.getId());
                    }
                }
            }
            
            poblacion.add(sol);
        }
        
        return poblacion;
    }
    
    // Verifica si un camión puede llevar un pedido adicional
    static boolean puedeAsignarPedido(Camion camion, List<Pedido> pedidosAsignados, Pedido nuevoPedido) {
        int cargaActual = pedidosAsignados.stream().mapToInt(Pedido::getCantidad).sum();
        return (cargaActual + nuevoPedido.getCantidad()) <= camion.getCapacidadEfectiva();
    }
    
    // Evalúa el fitness de toda la población (menor costo es mejor)
    static double evaluarPoblacion(List<Solucion> poblacion, List<Bloqueo> bloqueos, LocalDateTime fechaHora) {
        double sumaTotal=0;
        for (Solucion sol : poblacion) {
            double costo = sol.costoTotal(bloqueos, fechaHora);
            if (costo == Double.MAX_VALUE) {
                continue; // No sumar soluciones inválidas
            }
            sumaTotal += costo;
        }
        return sumaTotal;
    }
    
    // Selección por torneo
    static List<Solucion> seleccionarPadres(List<Solucion> poblacion, List<Bloqueo> bloqueos, LocalDateTime fechaHora) {
        List<Solucion> padres = new ArrayList<>();
        
        while (padres.size() < poblacion.size()) {
            // Seleccionar K individuos al azar
            List<Solucion> torneo = new ArrayList<>();
            for (int i = 0; i < TORNEOS_K; i++) {
                torneo.add(poblacion.get(rand.nextInt(poblacion.size())));
            }
            
            // El mejor del torneo (menor costo) es seleccionado
            padres.add(Collections.min(torneo, Comparator.comparingDouble(s -> s.costoTotal(bloqueos, fechaHora))));
        }
        
        return padres;
    }
    
    // Cruce de soluciones
    static List<Solucion> cruzar(List<Solucion> padres) {
        List<Solucion> descendientes = new ArrayList<>();
        
        for (int i = 0; i < padres.size(); i += 2) {
            if (i + 1 >= padres.size()) {
                descendientes.add(new Solucion(padres.get(i))); // Si hay número impar, el último pasa sin cruzar
                break;
            }
            
            Solucion padre1 = padres.get(i);
            Solucion padre2 = padres.get(i + 1);
            
            if (rand.nextDouble() < PROB_CRUCE) {
                // Cruzar
                Solucion hijo1 = new Solucion();
                Solucion hijo2 = new Solucion();
                
                // Cruzar las rutas de cada camión
                for (Camion camion : padre1.rutas.keySet()) {
                    List<Pedido> rutaPadre1 = padre1.rutas.get(camion);
                    List<Pedido> rutaPadre2 = padre2.rutas.get(camion);
                    
                    // Modificación importante: Manejar casos donde una o ambas rutas están vacías
                    int maxSize = Math.max(rutaPadre1.size(), rutaPadre2.size());
                    if (maxSize == 0) {
                        // Si ambas rutas están vacías, simplemente copiamos las vacías
                        hijo1.rutas.put(camion, new ArrayList<>(rutaPadre1));
                        hijo2.rutas.put(camion, new ArrayList<>(rutaPadre2));
                        continue;
                    }
                    
                    // Punto de cruce aleatorio (asegurando que al menos haya 1 elemento)
                    int puntoCruce = rand.nextInt(maxSize) + 1; // +1 para evitar 0 cuando maxSize es 1
                    
                    // Ajustar punto de cruce si es mayor que el tamaño de alguna ruta
                    puntoCruce = Math.min(puntoCruce, Math.max(rutaPadre1.size(), rutaPadre2.size()));
                    
                    // Crear rutas hijas
                    List<Pedido> rutaHijo1 = new ArrayList<>();
                    List<Pedido> rutaHijo2 = new ArrayList<>();
                    
                    // Añadir primera parte del padre1 al hijo1
                    if (!rutaPadre1.isEmpty()) {
                        rutaHijo1.addAll(rutaPadre1.subList(0, Math.min(puntoCruce, rutaPadre1.size())));
                    }
                    // Añadir segunda parte del padre2 al hijo1
                    if (!rutaPadre2.isEmpty()) {
                        rutaHijo1.addAll(rutaPadre2.subList(Math.min(puntoCruce, rutaPadre2.size()), rutaPadre2.size()));
                    }
                    
                    // Añadir primera parte del padre2 al hijo2
                    if (!rutaPadre2.isEmpty()) {
                        rutaHijo2.addAll(rutaPadre2.subList(0, Math.min(puntoCruce, rutaPadre2.size())));
                    }
                    // Añadir segunda parte del padre1 al hijo2
                    if (!rutaPadre1.isEmpty()) {
                        rutaHijo2.addAll(rutaPadre1.subList(Math.min(puntoCruce, rutaPadre1.size()), rutaPadre1.size()));
                    }
                    
                    // Asignar a los hijos
                    hijo1.rutas.put(camion, rutaHijo1);
                    hijo2.rutas.put(camion, rutaHijo2);
                }
                
                // Reparar soluciones (eliminar pedidos duplicados y asignar los faltantes)
                repararSolucion(hijo1, padre1, padre2);
                repararSolucion(hijo2, padre1, padre2);
                
                descendientes.add(hijo1);
                descendientes.add(hijo2);
            } else {
                // Pasar padres sin cruzar
                descendientes.add(new Solucion(padre1));
                descendientes.add(new Solucion(padre2));
            }
        }
        
        return descendientes;
    }
    // Repara una solución después del cruce (elimina duplicados y asigna pedidos faltantes)
    static void repararSolucion(Solucion solucion, Solucion padre1, Solucion padre2) {
        // Obtener todos los pedidos únicos de ambos padres
        Set<Pedido> todosPedidos = new HashSet<>();
        padre1.rutas.values().forEach(todosPedidos::addAll);
        padre2.rutas.values().forEach(todosPedidos::addAll);
        
        // Encontrar pedidos duplicados en la solución
        Map<Pedido, Integer> conteoPedidos = new HashMap<>();
        solucion.rutas.values().forEach(ruta -> {
            ruta.forEach(pedido -> {
                conteoPedidos.put(pedido, conteoPedidos.getOrDefault(pedido, 0) + 1);
            });
        });
        
        // Eliminar pedidos duplicados
        for (List<Pedido> ruta : solucion.rutas.values()) {
            Iterator<Pedido> it = ruta.iterator();
            while (it.hasNext()) {
                Pedido p = it.next();
                if (conteoPedidos.get(p) > 1) {
                    it.remove();
                    conteoPedidos.put(p, conteoPedidos.get(p) - 1);
                }
            }
        }
        
        // Encontrar pedidos faltantes
        Set<Pedido> pedidosActuales = new HashSet<>();
        solucion.rutas.values().forEach(pedidosActuales::addAll);
        List<Pedido> pedidosFaltantes = new ArrayList<>(todosPedidos);
        pedidosFaltantes.removeAll(pedidosActuales);
        
        // Asignar pedidos faltantes aleatoriamente
        Collections.shuffle(pedidosFaltantes);
        for (Pedido p : pedidosFaltantes) {
            boolean asignado = false;
            
            List<Camion> camionesConEspacio = solucion.rutas.keySet().stream()
            .filter(c -> c.getDisponible())
            .filter(c -> {
                int cargaActual = solucion.rutas.get(c).stream().mapToInt(Pedido::getCantidad).sum();
                return (cargaActual + p.getCantidad()) <= c.getCapacidadEfectiva();
            })
            .collect(Collectors.toList());
            
        if (!camionesConEspacio.isEmpty()) {
            // Asignar a un camión aleatorio con espacio
            Camion camion = camionesConEspacio.get(rand.nextInt(camionesConEspacio.size()));
            solucion.rutas.get(camion).add(p);
            asignado = true;
        }
            
            // Si no se pudo asignar (por capacidad), forzar en el camión con más espacio
            if (!asignado) {
                Camion mejorCamion = null;
                int espacioDisponible = -1;
                
                for (Camion camion : solucion.rutas.keySet()) {
                    if (!camion.getDisponible()) continue;
                    
                    int cargaActual = solucion.rutas.get(camion).stream().mapToInt(Pedido::getCantidad).sum();
                    int espacio = camion.getCapacidadEfectiva() - cargaActual;
                    if (espacio > espacioDisponible) {
                        espacioDisponible = espacio;
                        mejorCamion = camion;
                    }
                }
                
                if (mejorCamion != null) {
                    solucion.rutas.get(mejorCamion).add(p);
                }
            }
        }
    }
    
    // Mutación de soluciones
    static void mutar(List<Solucion> poblacion, List<Pedido> pedidos, List<Camion> camiones) {
        for (Solucion sol : poblacion) {
            if (rand.nextDouble() < PROB_MUTACION) {
                // Seleccionar un tipo de mutación aleatorio
                int tipoMutacion = rand.nextInt(3);
                
                switch (tipoMutacion) {
                    case 0: // Intercambiar dos pedidos entre camiones
                        mutarIntercambio(sol, camiones);
                        break;
                    case 1: // Mover un pedido a otro camión
                        mutarMovimiento(sol, camiones);
                        break;
                    case 2: // Reordenar pedidos dentro de un camión
                        mutarReordenamiento(sol);
                        break;
                }
            }
        }
    }
    
    // Mutación por intercambio de dos pedidos entre camiones
    static void mutarIntercambio(Solucion sol, List<Camion> camiones) {
        List<Camion> camionesDisponibles = camiones.stream()
        .filter(Camion::getDisponible)
        .collect(Collectors.toList());

        if (camionesDisponibles.size() < 2) return;
        
        // Seleccionar dos camiones aleatorios diferentes
        Camion camion1 = camionesDisponibles.get(rand.nextInt(camionesDisponibles.size()));
        Camion camion2;
        do {
            camion2 = camionesDisponibles.get(rand.nextInt(camionesDisponibles.size()));
        } while (camion1 == camion2);
        
        List<Pedido> ruta1 = sol.rutas.get(camion1);
        List<Pedido> ruta2 = sol.rutas.get(camion2);
        
        if (ruta1.isEmpty() || ruta2.isEmpty()) return;

        // Seleccionar pedidos aleatorios para intercambiar
        Pedido pedido1 = ruta1.get(rand.nextInt(ruta1.size()));
        Pedido pedido2 = ruta2.get(rand.nextInt(ruta2.size()));
        
        // Verificar si el intercambio es factible (capacidad)
        int cargaCamion1 = ruta1.stream().mapToInt(Pedido::getCantidad).sum() - pedido1.getCantidad() + pedido2.getCantidad();
        int cargaCamion2 = ruta2.stream().mapToInt(Pedido::getCantidad).sum() - pedido2.getCantidad() + pedido1.getCantidad();
        
        if (cargaCamion1 <= camion1.getCapacidadEfectiva() && 
            cargaCamion2 <= camion2.getCapacidadEfectiva()) {
            // Realizar el intercambio
            ruta1.remove(pedido1);
            ruta2.remove(pedido2);
            ruta1.add(pedido2);
            ruta2.add(pedido1);
        }
    }
    
    // Mutación por movimiento de un pedido a otro camión
    static void mutarMovimiento(Solucion sol, List<Camion> camiones) {
        List<Camion> camionesDisponibles = camiones.stream()
        .filter(Camion::getDisponible)
        .collect(Collectors.toList());
        
        if (camionesDisponibles.size() < 2) return;
        
        // Seleccionar camión origen aleatorio con al menos un pedido
        Camion camionOrigen;
        List<Pedido> rutaOrigen;
        do {
            camionOrigen = camionesDisponibles.get(rand.nextInt(camionesDisponibles.size()));
            rutaOrigen = sol.rutas.get(camionOrigen);
        } while (rutaOrigen.isEmpty());
        
        // Seleccionar camión destino diferente
        Camion camionDestino;
        do {
            camionDestino = camionesDisponibles.get(rand.nextInt(camionesDisponibles.size()));
        } while (camionOrigen == camionDestino);
        
        List<Pedido> rutaDestino = sol.rutas.get(camionDestino);
        Pedido pedido = rutaOrigen.get(rand.nextInt(rutaOrigen.size()));
        
        // Verificar si el movimiento es factible (capacidad)
        int nuevaCargaDestino = rutaDestino.stream().mapToInt(Pedido::getCantidad).sum() + pedido.getCantidad();
        if (nuevaCargaDestino <= camionDestino.getCapacidadEfectiva()) {
            rutaOrigen.remove(pedido);
            rutaDestino.add(pedido);
        }
    }
    
    // Mutación por reordenamiento de pedidos dentro de un camión
    static void mutarReordenamiento(Solucion sol) {
        // Seleccionar un camión aleatorio con al menos 2 pedidos
        List<Camion> camionesConPedidos = new ArrayList<>();
        for (Camion camion : sol.rutas.keySet()) {
            if (sol.rutas.get(camion).size() >= 2) {
                camionesConPedidos.add(camion);
            }
        }
        
        if (camionesConPedidos.isEmpty()) return;
        
        Camion camion = camionesConPedidos.get(rand.nextInt(camionesConPedidos.size()));
        List<Pedido> ruta = sol.rutas.get(camion);
        
        // Seleccionar dos índices diferentes
        int i = rand.nextInt(ruta.size());
        int j;
        do {
            j = rand.nextInt(ruta.size());
        } while (i == j);
        
        // Intercambiar pedidos
        Collections.swap(ruta, i, j);
    }
    
      // Métodos de carga de datos
    static List<Pedido> cargarPedidos(String archivo) throws IOException {
        List<Pedido> pedidos = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String linea;

        while ((linea = br.readLine()) != null) {
            // Omitir líneas vacías o comentarios (líneas que no empiezan con número)
            if (linea.trim().isEmpty() || !Character.isDigit(linea.trim().charAt(0))) {
                continue;
            }

            String[] partes = linea.split(":");
            String tiempo = partes[0];
            String datos = partes[1];

            // Parsear tiempo (ej. 01d12h25m)
            int dia = Integer.parseInt(tiempo.substring(0, 2));
            int hora = Integer.parseInt(tiempo.substring(3, 5));
            int minuto = Integer.parseInt(tiempo.substring(6, 8));
            int tiempoEnMinutos = hora + minuto * 60; // redondeo de minutos si deseas

            // Parsear datos después de los dos puntos
            String[] valores = datos.split(",");
            int ubicacionX = Integer.parseInt(valores[0]);
            int ubicacionY = Integer.parseInt(valores[1]);
            String idCliente = valores[2]; // quitar el 'c-'
            Cliente cliente = new Cliente(1, idCliente); // Asumiendo que tienes este constructor

            String cantidadStr = valores[3].replace("m3", "");
            int cantidad = Integer.parseInt(cantidadStr);

            String horaStr = valores[4].replace("h", "");
            int plazo = Integer.parseInt(horaStr);

            // Crear el pedido
            Pedido pedido = new Pedido();
            pedido.setId(pedidos.size() + 1); // Generar ID incremental
            pedido.setDia(dia);
            pedido.setCantidad(cantidad);
            pedido.setUbicacionX(ubicacionX);
            pedido.setUbicacionY(ubicacionY);
            pedido.setHora(tiempoEnMinutos); // Asignas el plazo (4h) a hora, o si prefieres usa otro campo
            pedido.setCliente(cliente);
            pedido.setEstado("pendiente"); // o lo que corresponda
            pedido.setTiempoMaximo(plazo);
            pedidos.add(pedido);
        }

        br.close();
        return pedidos;
    }

    static List<Camion> cargarCamiones(String archivo) throws IOException {
        List<Camion> camiones = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String linea;

        int[] contadorTipo = new int[Camion.TipoCamion.values().length]; // Para generar códigos únicos por tipo

        while ((linea = br.readLine()) != null) {
            if (linea.trim().isEmpty() || linea.trim().startsWith("#"))
                continue;

            String[] partes = linea.split(",");
            if (partes.length != 5) {
                System.err.println("Línea inválida (esperados 5 valores): " + linea);
                continue;
            }

            try {
                Camion.TipoCamion tipo = Camion.TipoCamion.valueOf(partes[0].trim());
                double pesoVacio = Double.parseDouble(partes[1].trim());
                int capacidadEfectiva = Integer.parseInt(partes[2].trim());
                double pesoMaximoCarga = Double.parseDouble(partes[3].trim());
                double _ = Double.parseDouble(partes[4].trim()); // este valor no se usa en la clase, pero lo ignoramos

                // Crear un código del tipo TA01, TB02, etc.
                int indice = tipo.ordinal();
                contadorTipo[indice]++;
                String codigo = String.format("%s%02d", tipo.name(), contadorTipo[indice]);

                Camion camion = new Camion(codigo, tipo, pesoVacio, capacidadEfectiva, pesoMaximoCarga);
                camiones.add(camion);
            } catch (Exception e) {
                System.err.println("Error al parsear línea: " + linea);
                e.printStackTrace();
            }
        }

        br.close();
        return camiones;
    }

    static List<Bloqueo> cargarBloqueos(String archivo) throws IOException {
        List<Bloqueo> bloqueos = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String linea;

        while ((linea = br.readLine()) != null) {
            // Omitir líneas vacías o comentarios
            if (linea.trim().isEmpty() || linea.trim().startsWith("#")) {
                continue;
            }

            // Dividir la línea en partes: tiempo y coordenadas
            String[] partes = linea.split(":");
            if (partes.length != 2) {
                System.err.println("Formato inválido en línea: " + linea);
                continue;
            }

            // Procesar el rango de tiempo
            String[] tiempos = partes[0].split("-");
            if (tiempos.length != 2) {
                System.err.println("Formato de tiempo inválido en línea: " + linea);
                continue;
            }

            // Parsear fecha/hora de inicio
            LocalDateTime inicio = parsearFechaHora(tiempos[0]);
            // Parsear fecha/hora de fin
            LocalDateTime fin = parsearFechaHora(tiempos[1]);

            // Procesar las coordenadas de los nodos bloqueados
            String[] coordenadas = partes[1].split(",");
            if (coordenadas.length % 2 != 0) {
                System.err.println("Número impar de coordenadas en línea: " + linea);
                continue;
            }

            List<Point> nodosBloqueados = new ArrayList<>();
            for (int i = 0; i < coordenadas.length; i += 2) {
                try {
                    int x = Integer.parseInt(coordenadas[i].trim());
                    int y = Integer.parseInt(coordenadas[i + 1].trim());
                    nodosBloqueados.add(new Point(x, y));
                } catch (NumberFormatException e) {
                    System.err.println("Coordenada inválida en línea: " + linea);
                }
            }

            // Crear el bloqueo y agregarlo al conjunto
            bloqueos.add(new Bloqueo(inicio, fin, nodosBloqueados));
        }

        br.close();
        return bloqueos;

    }
    // static Set<Integer> cargarMantenimiento(String archivo) throws IOException {
    // /* Implementar */ }

    private static LocalDateTime parsearFechaHora(String texto) {
        // Eliminar posibles espacios
        texto = texto.trim();

        // Dividir en días, horas, minutos
        String[] partes = texto.split("[dhm]");
        if (partes.length < 3) {
            throw new IllegalArgumentException("Formato de fecha inválido: " + texto);
        }

        // Obtener valores numéricos
        int dia = Integer.parseInt(partes[0]);
        int hora = Integer.parseInt(partes[1]);
        int minuto = Integer.parseInt(partes[2]);
        // Crear objeto LocalDateTime (asumiendo mes actual y año actual)
        return LocalDateTime.of(2025, Month.MAY, dia, hora, minuto, 0, 0);

    }


    public static List<Mantenimiento> cargaMantenimientos(String rutaArchivo) throws IOException {
        List<Mantenimiento> listaMantenimientos = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(rutaArchivo));
        String linea;

        while ((linea = br.readLine()) != null) {
            linea = linea.trim();
            if (linea.isEmpty() || linea.startsWith("#")) {
                continue; // Omitir líneas vacías o comentarios
            }

            String[] partes = linea.split(":");
            if (partes.length != 2) {
                System.err.println("Línea inválida: " + linea);
                continue; // Puedes lanzar excepción si prefieres
            }

            String fecha = partes[0]; // Ejemplo: 20250401
            String codigoCamion = partes[1].trim();

            int anho = Integer.parseInt(fecha.substring(0, 4));
            int mes = Integer.parseInt(fecha.substring(4, 6));
            int dia = Integer.parseInt(fecha.substring(6, 8));

            Mantenimiento mantenimiento = new Mantenimiento();
            mantenimiento.setAnho(anho);
            mantenimiento.setMes(mes);
            mantenimiento.setDia(dia);
            mantenimiento.setIdMantenimiento(1); // Usas el día como ID (si es así como lo quieres)

            mantenimiento.setCodigo(codigoCamion);

            listaMantenimientos.add(mantenimiento);
        }
        br.close();
        return listaMantenimientos;
    }

    public static void actualizarEstadoCamiones(LocalDateTime fechaHora, List<Camion> camiones, List<Mantenimiento> mantenimientos) {
        for (Mantenimiento m : mantenimientos) {
            LocalDate fechaMantenimiento = LocalDate.of(m.getAnho(), m.getMes(), m.getDia());
    
            if (fechaHora.toLocalDate().equals(fechaMantenimiento)) {
                for (Camion camion : camiones) {
                    if (camion.getCodigo().equals(m.getCodigo())) {
                        camion.setDisponible(false);
                        System.out.println("Camión " + camion.getCodigo() + " en mantenimiento el " + fechaHora);
                        break;
                    }
                }
            }
        }
    }
    
}

